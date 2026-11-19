package br.com.olivaras.api.divination;

import br.com.olivaras.api.catalog.dto.CatalogItemResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/profile/wand-divination")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Adivinhação")
public class WandDivinationController {
    private final WandDivinationService divination;

    public WandDivinationController(WandDivinationService divination) { this.divination = divination; }

    @GetMapping("/questions")
    public List<WandQuiz.Question> questions() { return WandQuiz.QUESTIONS; }

    @GetMapping
    public ResponseEntity<CatalogItemResponse> current(@AuthenticationPrincipal Jwt jwt) {
        return divination.current(UUID.fromString(jwt.getSubject()))
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public CatalogItemResponse reveal(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Answers request) {
        return divination.reveal(UUID.fromString(jwt.getSubject()), request.answers());
    }

    @DeleteMapping("/{wandId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID wandId) {
        divination.destroy(UUID.fromString(jwt.getSubject()), wandId);
    }

    public record Answers(@NotNull @Size(min = WandQuiz.QUESTION_COUNT, max = WandQuiz.QUESTION_COUNT)
                          List<@NotNull @Min(0) @Max(3) Integer> answers) {}
}
