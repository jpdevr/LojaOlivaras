package br.com.olivaras.api.catalog.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogImageStorage {
    private static final Logger LOG = LoggerFactory.getLogger(CatalogImageStorage.class);
    private static final String PUBLIC_PATH = "/lojaolivaras/api/catalogo/imagens/";
    private static final int MAX_SIDE = 4096;
    private final Path directory;

    public CatalogImageStorage(@Value("${app.catalog.image-directory}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public List<String> store(List<MultipartFile> images) {
        if (images.isEmpty() || images.size() > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie de uma a três imagens");
        }

        List<Path> written = new ArrayList<>(images.size());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_ROLLED_BACK) return;
                for (Path path : written) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException exception) {
                        LOG.warn("Não foi possível remover uma imagem após cancelar o cadastro", exception);
                    }
                }
            }
        });

        List<String> references = new ArrayList<>(images.size());
        for (MultipartFile image : images) {
            DecodedImage decoded = decode(image);
            String name = UUID.randomUUID() + "." + ("jpeg".equals(decoded.format()) ? "jpg" : "png");
            Path path = directory.resolve(name);
            try {
                Files.createDirectories(directory);
                try (var output = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW)) {
                    written.add(path);
                    if (!ImageIO.write(decoded.content(), decoded.format(), output)) {
                        throw new IOException("Codificador de imagem indisponível");
                    }
                }
            } catch (IOException exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Não foi possível armazenar a imagem", exception);
            }
            references.add(PUBLIC_PATH + name);
        }
        return references;
    }

    public Resource load(UUID id, String extension) {
        Path path = directory.resolve(id + "." + extension);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
        }
        return new FileSystemResource(path);
    }

    private DecodedImage decode(MultipartFile image) {
        try (var raw = image.getInputStream(); var input = ImageIO.createImageInputStream(raw)) {
            if (input == null) throw invalidImage();
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalidImage();
            var reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!"jpeg".equals(format) && !"png".equals(format)) throw invalidImage();
                reader.setInput(input, true, true);
                if (reader.getWidth(0) > MAX_SIDE || reader.getHeight(0) > MAX_SIDE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A imagem pode ter no máximo 4096 × 4096 pixels");
                }
                return new DecodedImage(reader.read(0), format);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw invalidImage();
        }
    }

    private ResponseStatusException invalidImage() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo PNG ou JPEG válido");
    }

    private record DecodedImage(BufferedImage content, String format) {}
}
