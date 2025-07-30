package com.openclassrooms.tourguide;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TourGuideApplicationTests {

    @Test
    void contextLoads() {
		/*  Ce test vérifie une seule chose : "Est-ce que le contexte Spring Boot démarre sans erreur ?"
            Même si la méthode est vide, si quelque chose empêche le démarrage
            (erreur de configuration, bean manquant...), le test échouera.
            S'il passe, cela signifie que l’application peut démarrer correctement dans un environnement de test.

            ✅ À quoi ça sert en pratique ?
            C’est souvent le premier test exécuté dans une suite de tests pour s'assurer que tout est correctement câblé.
            Il est très utile dans les CI/CD (GitLab, GitHub Actions, Jenkins...) pour valider que la base de ton
            application est saine.
		 */
    }

}
