package br.com.olivaras.api.divination;

import br.com.olivaras.api.catalog.entity.WandProductEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class WandQuiz {
    public static final int QUESTION_COUNT = 10;

    public record Question(String prompt, List<String> options) {
        private Question(String prompt, String... options) { this(prompt, List.of(options)); }
    }

    public static final List<Question> QUESTIONS = List.of(
            new Question("O que você procura ao abrir um mapa antigo?",
                    "Um jardim esquecido", "A entrada de uma cripta", "Um bosque acima das nuvens", "Uma cidade de forjas"),
            new Question("Que som você seguiria com os olhos fechados?",
                    "O passo de alguém na pedra", "O vento entre as copas", "Um martelo na bigorna", "Um sino do outro lado do lago"),
            new Question("Uma noite livre na escola de magia. Onde você vai parar?",
                    "Nos telhados, para ver o céu", "Na oficina, para terminar uma invenção",
                    "No observatório, para desenhar estrelas", "Na estufa, entre as plantas adormecidas"),
            new Question("O que você faria com uma chave que abre qualquer porta?",
                    "Abriria a oficina de um mestre artesão", "Buscaria uma passagem para lugares impossíveis",
                    "Reabriria um jardim para toda a vila", "Investigaria uma porta que ninguém ousa tocar"),
            new Question("Qual presente você guardaria por toda a vida?",
                    "Um espelho com uma constelação", "Uma muda de uma árvore querida",
                    "Um anel escuro com um segredo", "Uma rosa dos ventos esculpida em madeira"),
            new Question("Uma luz surge na mata. Como ela brilha?",
                    "Como um enxame dourado", "Como fios de prata", "Como um clarão azul", "Como brasas sob as folhas"),
            new Question("Qual feitiço você gostaria de dominar primeiro?",
                    "Uma luz que guia quem se perdeu", "Um véu que esconde trilhas",
                    "Um chamado que reúne as nuvens", "Uma chama que aquece sem consumir"),
            new Question("Que objeto você levaria para explorar uma ruína?",
                    "Uma lanterna de âmbar", "Um carretel de fio prateado",
                    "Um cristal que vibra antes da chuva", "Uma pedra que conserva o calor do dia"),
            new Question("Como você faria o primeiro gesto de um feitiço?",
                    "Com um toque curto do pulso", "Com uma curva precisa no ar",
                    "Com um movimento amplo do braço", "Traçando um grande arco acima da cabeça"),
            new Question("Quando um feitiço falha, o que você tenta em seguida?",
                    "Repito o mesmo movimento até acertar", "Ajusto um detalhe de cada vez",
                    "Troco de técnica e observo o que muda", "Improviso um feitiço novo a partir do erro")
    );

    private static final List<String> FLEXIBILITIES = List.of("Rígida", "Equilibrada", "Flexível", "Muito flexível");
    private static final BigDecimal STARTING_LENGTH = new BigDecimal("9.75");
    private static final BigDecimal LENGTH_STEP = new BigDecimal("1.25");

    private enum Wood {
        MYRTLE("Murta prateada", "A murta prateada favorece a atenção ao que cresce em silêncio."),
        EBONY("Ébano", "O ébano responde à firmeza de quem prefere observar antes de agir."),
        ARAUCARIA("Araucária", "A araucária acompanha a curiosidade de quem sempre procura um horizonte mais alto."),
        IPE("Ipê rubro", "O ipê rubro responde ao impulso de transformar uma ideia em algo concreto."),
        POPLAR("Álamo lilás", "O álamo lilás favorece a imaginação de quem encontra passagens onde outros veem paredes.");

        private final String material;
        private final String description;

        Wood(String material, String description) {
            this.material = material;
            this.description = description;
        }
    }

    private enum Core {
        AMBER("Âmbar de vaga-lume", "O núcleo de âmbar de vaga-lume concentra um brilho constante, mesmo sob ventos fortes."),
        SILK("Seda de aranha-lunar", "A seda de aranha-lunar refina encantamentos delicados e deixa um traço prateado no ar."),
        CRYSTAL("Cristal de tempestade", "O cristal de tempestade desperta em gestos decididos e vibra quando a chuva se aproxima."),
        EMBER("Escama de salamandra", "A escama de salamandra guarda calor e dá vigor a encantamentos de proteção.");

        private final String material;
        private final String description;

        Core(String material, String description) {
            this.material = material;
            this.description = description;
        }
    }

    private WandQuiz() {}

    static WandProductEntity reveal(UUID customerId, String ownerName, List<Integer> answers) {
        int[] woodVotes = new int[Wood.values().length];
        int[] coreVotes = new int[Core.values().length];
        // The four wood choices start at a different material on each of the first five questions.
        for (int index = 0; index < 5; index++) {
            woodVotes[(index + answers.get(index)) % woodVotes.length]++;
        }
        for (int index = 5; index < 8; index++) coreVotes[answers.get(index)]++;

        Wood wood = Wood.values()[strongest(woodVotes, (4 + answers.get(4)) % woodVotes.length)];
        Core core = Core.values()[strongest(coreVotes, answers.get(5))];
        BigDecimal length = STARTING_LENGTH.add(LENGTH_STEP.multiply(BigDecimal.valueOf(answers.get(8))));
        int artNumber = wood.ordinal() * coreVotes.length + core.ordinal() + 1;
        String imageUrl = String.format(Locale.ROOT, "/assets/wands/personalizadas/varinha-%02d.webp", artNumber);
        return WandProductEntity.personalWand(customerId, ownerName, wood.material, core.material,
                length, FLEXIBILITIES.get(answers.get(9)), wood.description + " " + core.description, imageUrl);
    }

    private static int strongest(int[] votes, int preferred) {
        int winner = preferred;
        for (int index = 0; index < votes.length; index++) {
            if (votes[index] > votes[winner]) winner = index;
        }
        return winner;
    }
}
