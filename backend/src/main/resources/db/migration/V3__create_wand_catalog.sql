CREATE TABLE wand_product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    owner_name VARCHAR(120),
    wood VARCHAR(80),
    core VARCHAR(120),
    length_inches NUMERIC(5, 2),
    flexibility VARCHAR(100),
    maker VARCHAR(120),
    description TEXT NOT NULL,
    canon_tier VARCHAR(40) NOT NULL,
    source_reference VARCHAR(500) NOT NULL,
    price_galleons NUMERIC(12, 2) NOT NULL CHECK (price_galleons >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image_url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT wand_product_canon_tier_ck CHECK (
        canon_tier IN ('BOOKS', 'OFFICIAL_SUPPLEMENTAL', 'FILM_PRODUCTION')
    )
);

CREATE INDEX wand_product_active_name_idx ON wand_product (active, name);
CREATE INDEX wand_product_active_wood_idx ON wand_product (active, wood);
CREATE INDEX wand_product_active_core_idx ON wand_product (active, core);

-- Seed academic catalog: commerce values are illustrative; unknown lore fields remain NULL.
INSERT INTO wand_product
    (slug, name, owner_name, wood, core, length_inches, flexibility, maker, description,
     canon_tier, source_reference, price_galleons, stock)
VALUES
    ('harry-potter-holly', 'Varinha de Harry Potter', 'Harry Potter', 'Azevinho', 'Pena de fênix (Fawkes)', 11.00, 'Flexível', 'Garrick Ollivander', 'Varinha de azevinho com núcleo de pena de fênix. O núcleo compartilha origem com a varinha de Voldemort.', 'BOOKS', 'https://www.harrypotter.com/features/which-wand-is-connected-to-the-harry-potter-character', 75.00, 8),
    ('tom-riddle-yew', 'Varinha de Tom Riddle', 'Tom Riddle / Lord Voldemort', 'Teixo', 'Pena de fênix (Fawkes)', 13.50, NULL, 'Garrick Ollivander', 'Varinha de teixo com núcleo de pena da mesma fênix ligada à varinha de Harry Potter.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 82.00, 5),
    ('hermione-granger-vine', 'Varinha de Hermione Granger', 'Hermione Granger', 'Videira', 'Fibra de coração de dragão', NULL, NULL, 'Garrick Ollivander', 'Varinha de videira com fibra de coração de dragão. Comprimento e flexibilidade não foram preenchidos por falta de confirmação consistente.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 72.00, 7),
    ('ron-weasley-willow', 'Segunda varinha de Ron Weasley', 'Ron Weasley', 'Salgueiro', 'Pelo de unicórnio', 14.00, NULL, 'Garrick Ollivander', 'Varinha comprada para Ron após a perda de sua primeira varinha; madeira de salgueiro e núcleo de pelo de unicórnio.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 68.00, 6),
    ('ron-weasley-ash', 'Primeira varinha de Ron Weasley', 'Ron Weasley (herdada de Charlie)', 'Freixo', NULL, NULL, NULL, NULL, 'Varinha de segunda mão usada por Ron e quebrada no segundo ano. O núcleo e as demais medidas não foram confirmados.', 'OFFICIAL_SUPPLEMENTAL', 'https://www.hp-lexicon.org/thing/wands/', 42.00, 3),
    ('rubeus-hagrid-oak', 'Varinha de Rubeus Hagrid', 'Rubeus Hagrid', 'Carvalho', NULL, 16.00, 'Bastante flexível', 'Garrick Ollivander (atribuído)', 'Varinha longa de carvalho. Parte da varinha foi escondida no guarda-chuva de Hagrid; núcleo não confirmado.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 88.00, 3),
    ('fleur-delacour-rosewood', 'Varinha de Fleur Delacour', 'Fleur Delacour', 'Roseira', 'Cabelo de veela', 9.50, 'Inflexível', 'Garrick Ollivander', 'Varinha com cabelo de veela proveniente da avó de Fleur.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 70.00, 4),
    ('cedric-diggory-unicorn', 'Varinha de Cedric Diggory', 'Cedric Diggory', NULL, 'Pelo de unicórnio', 12.25, 'Agradavelmente elástica', 'Garrick Ollivander', 'O comprimento e o núcleo são registrados; a madeira não foi confirmada na fonte consultada.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 69.00, 4),
    ('viktor-krum-hornbeam', 'Varinha de Viktor Krum', 'Viktor Krum', 'Carpino', 'Fibra de coração de dragão', 10.25, 'Bastante rígida', 'Gregorovitch', 'Varinha de carpino fabricada por Gregorovitch.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 73.00, 4),
    ('bellatrix-lestrange-walnut', 'Varinha de Bellatrix Lestrange', 'Bellatrix Lestrange', 'Nogueira', 'Fibra de coração de dragão', 12.75, 'Inflexível', 'Garrick Ollivander (atribuído)', 'Varinha de nogueira descrita como inflexível; foi tomada por Ron na Mansão Malfoy.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 79.00, 3),
    ('draco-malfoy-hawthorn', 'Varinha de Draco Malfoy', 'Draco Malfoy', 'Espinheiro-alvar', 'Pelo de unicórnio', 10.00, 'Razoavelmente elástica', 'Garrick Ollivander', 'Varinha de espinheiro-alvar com núcleo de pelo de unicórnio.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 71.00, 4),
    ('peter-pettigrew-chestnut', 'Varinha de Peter Pettigrew', 'Peter Pettigrew', 'Castanheiro', 'Fibra de coração de dragão', 9.25, 'Frágil', 'Garrick Ollivander', 'Varinha de castanheiro que Ollivander foi forçado a fabricar para Pettigrew.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 65.00, 3),
    ('garrick-ollivander-hornbeam', 'Varinha de Garrick Ollivander', 'Garrick Ollivander', 'Carpino', 'Fibra de coração de dragão', 12.75, 'Levemente flexível', 'Garrick Ollivander', 'Varinha de carpino e fibra de coração de dragão, descrita em material suplementar oficial.', 'OFFICIAL_SUPPLEMENTAL', 'https://www.hp-lexicon.org/thing/wands/', 78.00, 2),
    ('james-potter-mahogany', 'Varinha de James Potter', 'James Potter', 'Mogno', NULL, 11.00, 'Flexível', NULL, 'Varinha de mogno associada a bons resultados em Transfiguração; núcleo não confirmado.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 67.00, 3),
    ('lily-potter-willow', 'Varinha de Lily Potter', 'Lily Potter', 'Salgueiro', NULL, 10.25, 'Bastante maleável', 'Garrick Ollivander (atribuído)', 'Varinha de salgueiro; núcleo não confirmado.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 67.00, 3),
    ('elder-wand', 'Varinha das Varinhas', 'Albus Dumbledore / Gellert Grindelwald / Harry Potter', 'Sabugueiro', 'Pelo de cauda de testrálio', NULL, NULL, 'Antiga; fabricante desconhecido', 'Relíquia da Morte conhecida por sua longa cadeia de proprietários. Comprimento e flexibilidade não foram confirmados aqui.', 'BOOKS', 'https://www.harrypotter.com/fact-file/objects/wand', 120.00, 1),
    ('neville-longbottom-cherry', 'Segunda varinha de Neville Longbottom', 'Neville Longbottom', 'Cerejeira', 'Pelo de cauda de unicórnio', NULL, NULL, 'Garrick Ollivander (atribuído)', 'Varinha adquirida por Neville após a quebra da varinha herdada do pai; comprimento e flexibilidade não confirmados.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 66.00, 3),
    ('dolores-umbridge-birch', 'Varinha de Dolores Umbridge', 'Dolores Umbridge', 'Bétula', 'Fibra de coração de dragão', 8.00, NULL, 'Garrick Ollivander (atribuído)', 'Varinha curta de bétula. A flexibilidade não foi confirmada na fonte consultada.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 64.00, 2),
    ('lucius-malfoy-elm', 'Varinha de Lucius Malfoy', 'Lucius Malfoy', 'Olmo', 'Fibra de coração de dragão', NULL, NULL, 'Garrick Ollivander (atribuído)', 'A madeira e o núcleo são registrados; comprimento e flexibilidade não foram confirmados.', 'BOOKS', 'https://www.hp-lexicon.org/thing/wands/', 74.00, 3),
    ('newt-scamander-ash', 'Varinha de Newt Scamander', 'Newt Scamander', 'Freixo', NULL, NULL, NULL, NULL, 'Varinha do filme descrita pela equipe de produção com madeira de freixo e detalhe de concha. O núcleo não foi divulgado.', 'FILM_PRODUCTION', 'https://time.com/4437196/eddie-redmayne-had-a-very-specific-condition-for-his-fantastic-beasts-and-where-to-find-them-wand/', 76.00, 3);
