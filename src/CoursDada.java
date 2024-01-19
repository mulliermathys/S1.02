
import extensions.File;
import extensions.CSVFile;

class CoursDada extends Program { 

    final String CHEMIN_FICHIER_SAUVEGARDE = "../sauvegardes/sauvegardeJoueurs.csv";
    final int IDX_NOM_SAUVEGARDE = 0;
    final int IDX_POSITION_SAUVEGARDE = 1;
    final int IDX_MEILLEUR_SCORE_SAUVEGARDE = 2;
    final int IDX_SCORE_COURANT_SAUVEGARDE = 3;

    final int NOMBRE_CASE_PLATEAU = 12;

    final int[] THEMES_CASES = genererThemesCase();
    final String TITRE_JEU = utiliserCouleur("vert") + supprimerCaractereIdx(length(lireFichier("../patterns/titre.txt")) - 1, lireFichier("../patterns/titre.txt")) + utiliserCouleur("reset");
    final String[] FACES_DE = recupererFacesDe();

    /**
     ****** Fonction d'algorithme principal ******
     */
    void algorithm() {
        /**
         * Récupération des données du jeu
         */
        fichierSauvegardeEstCree();
        String[][] contenuSauvegarde = recupererContenuCSV(CHEMIN_FICHIER_SAUVEGARDE);

        /**
         * Lancement du jeu et récupération des données correspondantes aux joueur
         */    
        passerLignes(50);
        clearScreen();
        String nom = lancerJeu();
        Joueur joueur = affecterJoueur(contenuSauvegarde, nom);
        contenuSauvegarde = recupererContenuCSV(CHEMIN_FICHIER_SAUVEGARDE);
        afficherDebutJeu(joueur);
        String[][] casesPlateau = genererCasesPlateau(joueur.position, THEMES_CASES);
        String plateau = assemblerPlateau(casesPlateau);
        String affichageDe = "";
        dessinerJeu(TITRE_JEU, plateau, affichageDe, joueur.score);

        /**
         * Attente du lancement et lancement de la boucle de jeu 
         */
        println("Appuyez sur la touche \"Entrée\" pour lancer le dé");
        String attente = readString();
        boolean fini = false;
        boolean running = true;

        /**
         * Boucle de jeu
         */
        while (running) {
            joueur.score += 1;
            int valeurDe = entierRandom(1, 7);
            affichageDe = utiliserCouleur("rouge") + FACES_DE[valeurDe - 1] + utiliserCouleur("reset");
            deplacerJoueur(joueur, valeurDe);

            /**
             * Vérification que le joueur n'as pas atteint la case d'arrivée
             */
            if (joueur.position == (NOMBRE_CASE_PLATEAU - 1)) {
                running = false;
                fini = true;
            }

            casesPlateau = genererCasesPlateau(joueur.position, THEMES_CASES);
            plateau = assemblerPlateau(casesPlateau);
            clearScreen();
            dessinerJeu(TITRE_JEU, plateau, affichageDe, joueur.score);
            println("Tu viens d'avancer de " + valeurDe + " cases ! ");

            if (!equals(toUpperCase(attente), toUpperCase("sortie"))) {
                boolean bonneReponse = true;
                String reponseJoueur = "";
                Question question = newQuestion(0, "", "", "", new String[0][0]);
                if ((joueur.position > 0) && (joueur.position < (NOMBRE_CASE_PLATEAU - 1))) {
                    String matiere = obtenirMatiereCase((joueur.position - 1), THEMES_CASES);
                    question = obtenirQuestionMatiere(matiere);
                    poserQuestion(question);
                    reponseJoueur = readString();
                    bonneReponse = estBonneReponse(question, reponseJoueur);
                }

                if (!bonneReponse) {
                    if (!equals(reponseJoueur, "sortie")) {
                        mauvaiseReponse(joueur, question, reponseJoueur, affichageDe);
                    } else {
                        running = false;
                    }
                } else if (bonneReponse) {
                    println("Bonne réponse ! Tu gardes ta place :)");
                }

                println("Appuyez sur la touche \"Entrée\" pour relancer le dé");
                if (!equals(reponseJoueur, "sortie")) {
                    attente = readString();
                }
            } else {
                running = false;
            }
            clearScreen();
        }

        if (fini) {
            finDePartie(joueur, contenuSauvegarde);
        } else {
            sauvegarderPartie(joueur, contenuSauvegarde);
            println("A bientôt !");
        }
    }

    /**
     ****** Fonction d'affichage du jeu ******
     */

    /**
     * La fonction afficherAccueil affiche la page d'accueil du jeu (/!\ Page à embellir /!\)
     */
    void afficherAccueil() {
        println("*** Bienvenue dans CoursDada ! ***");
        print("Entre ton nom de joueur (Sans virgules ! ) : ");
    }
    
    /**
     * La fonction passerLignes permet de laisser autant de lignes vides que le nombre donné en 
     * paramètre
     */
    void passerLignes(int nbLignesAPasser) {
        for (int cptLigne = 0; cptLigne < nbLignesAPasser; cptLigne++) {
            print('\n');
        }
    }

    /**
     * La fonction recupererFacesDe renvoie un tableau de chaines de caractères. Chaque chaine de
     * caractères de ce tableau correspond à la face d'un dé (face de valeur 1, ..., face de valeur 6)
     */
    String[] recupererFacesDe() {
        String chaineDes = lireFichier("../patterns/de_pattern.txt");
        String[] tableauFacesDe = new String[6];
        String chaine = "";
        int cpt = 0;
        for (int idxChaineDes = 0; idxChaineDes < (length(chaineDes) - 1); idxChaineDes++) {
            if (((charAt(chaineDes, idxChaineDes) == '\n') && (charAt(chaineDes, idxChaineDes + 1) == '\n')) || (idxChaineDes == (idxChaineDes - 3))) {
                tableauFacesDe[cpt] = chaine;
                cpt++;
                chaine = "";
            } else if (!(equals(chaine,"") && (charAt(chaineDes, idxChaineDes) == '\n'))) {
                chaine = chaine + charAt(chaineDes, idxChaineDes);
            }
        }
        tableauFacesDe[5] = chaine;
        return tableauFacesDe;
    }

    /**
     * La fonction afficherDebutJeu affiche une fenêtre intérmédiaire avant de commencer le jeu
     */
    void afficherDebutJeu(Joueur joueur) {
        passerLignes(1);
        println("Bienvenue " + joueur.nom + " !");
        if (joueur.meilleurScore > 0) {
            println("Ton meilleur score est de : " + joueur.meilleurScore + " ! ");
            println("Arriveras-tu à le battre ? ");
        } else {
            println("Oh on dirait bien que c'est ta première partie ! ");
        }
        passerLignes(1);
        println("Clique sur la touche \"Entrée\" pour lancer la partie ! ");
        readString();
        clearScreen();
    }

    /**
     * La fonction dessinerJeu() affiche le jeu
     */
    void dessinerJeu(String titre, String plateau, String affichageDe, int score) {
        println(titre);
        passerLignes(3);
        println(plateau);
        passerLignes(1);
        if (!equals(affichageDe, "")) {
            println(affichageDe);
            passerLignes(1);
        }
        println("Score : " + score + " - Si vous souhaitez enregistrer votre partie et quitter le jeu maintenant, tape \"Sortie\" et valide");
        passerLignes(1);
    }

    /**
     * La fonction utiliserCouleur renvoie le code couleur ANSI de la couleur sous forme de chaine de 
     * caractère à concatener avec avec la chaine à colorer
     */
    String utiliserCouleur(String couleur) {
        couleur = toUpperCase(couleur);
        if (equals(couleur, "RESET")) {
            return "\u001B[0m";
        } else if (equals(couleur, "NOIRE")) {
            return "\u001B[30m";
        } else if (equals(couleur, "ROUGE")) {
            return "\u001B[31m";
        } else if (equals(couleur, "VERT")) {
            return "\u001B[32m";
        } else if (equals(couleur, "JAUNE")) {
            return "\u001B[33m";
        } else if (equals(couleur, "BLEU")) {
            return "\u001B[34m";
        } else if (equals(couleur, "VIOLET")) {
            return "\u001B[35m";
        } else if (equals(couleur, "CYAN")) {
            return "\u001B[36m";
        } else if (equals(couleur, "GRIS")) {
            return "\u001B[37m";
        } else {
            return "";
        }
    }

    void testUtiliserCouleur() {
        assertEquals("\u001B[32m", utiliserCouleur("vert"));
        assertEquals("\u001B[0m", utiliserCouleur("reset"));
        assertEquals("", utiliserCouleur("N'importe quoi"));
    }

    /**
     ****** Fonctions de découpage du jeu ******
     */

    /**
     * La fonction lancerJeu affiche la page d'accueil du jeu et renvoie un nom valide pour le joueur
     */
    String lancerJeu() {
        afficherAccueil();
        String nom = readString();
        while (!estPrenomValide(nom)) {
            println("Ton prénom est invalide :/ Oublie pas que les virgules ne sont pas acceptées ! ");
            print("Allez on recommence ! Entre un nom valide : ");
            nom = readString();
        }
        return nom;
    }

    /**
     * La fonction finDePartie réalise les tâches à réaliser lorsque le joueur a fini la partie en
     * atteignant la case d'arrivée et affiche un message de fin 
     */
    void finDePartie(Joueur joueur, String[][] contenuSauvegarde) {
        String messageFelicitations  = "BRAVO !\n\nTu as réussi à finir de ce jeu ! ";
            if ((joueur.score < joueur.meilleurScore) || (joueur.meilleurScore == -1)) {
                if (joueur.score < joueur.meilleurScore) {
                    messageFelicitations    += "Encore mieux tu as battu ton meilleur record qui était de " 
                                            + joueur.meilleurScore + " en faisant un score de " 
                                            + joueur.score + " ! ";
                }
                joueur.meilleurScore = joueur.score;
            }
            if (joueur.position == (NOMBRE_CASE_PLATEAU - 1)) {
                joueur.position = 0;
                joueur.score = 0;
            }
        sauvegarderPartie(joueur, contenuSauvegarde);
        messageFelicitations += "A bientot pour de nouvelles aventures :)";
        println(messageFelicitations);
    }

    /**
     * La fonction mauvaiseReponse réalise les tâches opérations nécessaires si le joueur donne une
     * mauvaise réponse
     */
    void mauvaiseReponse(Joueur joueur, Question question, String reponseJoueur, String affichageDe) {
        joueur.position = 0;
        String[][] casesPlateau = genererCasesPlateau(joueur.position, THEMES_CASES);
        String plateau = assemblerPlateau(casesPlateau);
        clearScreen();
        dessinerJeu(TITRE_JEU, plateau, affichageDe, joueur.score);
        println("Mauvaise réponse :/ Retour à la case départ !");
        println("Tu as répondu : " + reponseJoueur);
        println("La bonne réponse était : " + question.reponse);
    }

    /**
     * La fonction poser question récupère l'intitulé et le thème d'une question et l'affiche
     */
    void poserQuestion(Question question) {
        renommerMatiereQuestion(question);
        String texteQuestion = "Tu es tombé sur une question " + question.matiere + " !\n\n";
        texteQuestion = texteQuestion + question.intitule + '\n';
        println(texteQuestion);
    }

    /**
     ****** Fonctions liées à la sauvegarde de la partie du joueur. ******
     */

    /**
     * La fonction fichierSauvegardeEstCree vérifie si une sauvegarde du jeu existe dans le bon 
     * répertoire. Si le fichier n'existe pas la fonction appelle la fonction creerCSVSauvegardeJoueurs
     */
    void fichierSauvegardeEstCree() {
        final String NOM_FICHIER_SAUVEGARDE = "sauvegardeJoueurs.csv";
        final String CHEMIN_DOSSIER_SAUVEGARDE = "../sauvegardes";
        String[] allFilesFromDirectory = getAllFilesFromDirectory(CHEMIN_DOSSIER_SAUVEGARDE);
        boolean estCree = false;
        int indice = 0;
        while (indice < length(allFilesFromDirectory) && !estCree) {
            if (equals(allFilesFromDirectory[indice], NOM_FICHIER_SAUVEGARDE)) {
                estCree = true;
            }
            indice = indice + 1;
        }
        if (!estCree) {
            creerCSVSauvegardeJoueurs();
        }
    }

    /**
     * La fonction creerCSVSauvegardeJoueurs créer un fichier CSV de sauvegarde dans le bon répertoire
     */
    void creerCSVSauvegardeJoueurs() {
        String[][] joueursCSV = new String[1][4];
        joueursCSV[0][IDX_NOM_SAUVEGARDE] = "nomDuJoueur";
        joueursCSV[0][IDX_POSITION_SAUVEGARDE] = "positionDernièrePartie";
        joueursCSV[0][IDX_MEILLEUR_SCORE_SAUVEGARDE] = "meilleurScore";
        joueursCSV[0][IDX_SCORE_COURANT_SAUVEGARDE] = "scoreCourant";
        saveCSV(joueursCSV, CHEMIN_FICHIER_SAUVEGARDE);
        println("Fichier créé");
    }

    /**
     * La fonction recupererContenuCSV récupère le contenu d'un fichier CSV et ses élments dans un 
     * tableau à doubles dimensions
     */
    String[][] recupererContenuCSV(String cheminFichier) {
        CSVFile table = loadCSV(cheminFichier);
        int nombreLigneTable = rowCount(table);
        int nombreColonneTable = columnCount(table);
        String[][] contenuTable = new String[nombreLigneTable][nombreColonneTable];
        for (int idxLigne = 0; idxLigne < nombreLigneTable; idxLigne++) {
            for (int idxColonne = 0; idxColonne < nombreColonneTable; idxColonne++) {
                contenuTable[idxLigne][idxColonne] = getCell(table, idxLigne, idxColonne);
            }
        }
        return contenuTable;
    }

    void testRecupererContenuCSV() {
        final String CHEMIN_FICHIER_TEST = "../autres/fichierDeTestCSV.csv";
        String[][] contenuTest = recupererContenuCSV(CHEMIN_FICHIER_TEST);
        int cpt = 1;
        for (int idxLigne = 0; idxLigne < length(contenuTest); idxLigne++) {
            for (int idxColonne = 0; idxColonne < length(contenuTest[idxLigne]); idxColonne++) {
                assertEquals("test" + cpt, contenuTest[idxLigne][idxColonne]);
                cpt = cpt + 1;
            }
        }
    }
    /**
     * La fonction ajouterJoueurASauvegarde ajoute un nouveau joueur au fichier de sauvegarde
     */
    void ajouterJoueurASauvegarde(Joueur joueur, String[][] contenuSauvegarde) {
        int nombreLigneTable = length(contenuSauvegarde);
        String[][] contenuNouvelleSauvegarde = new String[nombreLigneTable + 1][IDX_SCORE_COURANT_SAUVEGARDE + 1];

        for (int idxLigne = 0; idxLigne < nombreLigneTable; idxLigne++) {
            contenuNouvelleSauvegarde[idxLigne] = contenuSauvegarde[idxLigne];
        }

        contenuNouvelleSauvegarde[nombreLigneTable][IDX_NOM_SAUVEGARDE] = joueur.nom;
        contenuNouvelleSauvegarde[nombreLigneTable][IDX_POSITION_SAUVEGARDE] = "" + joueur.position;
        contenuNouvelleSauvegarde[nombreLigneTable][IDX_MEILLEUR_SCORE_SAUVEGARDE] = "" + joueur.meilleurScore;
        contenuNouvelleSauvegarde[nombreLigneTable][IDX_SCORE_COURANT_SAUVEGARDE] = "" + joueur.score;

        saveCSV(contenuNouvelleSauvegarde, CHEMIN_FICHIER_SAUVEGARDE);
    }

    /**
     * La fonction sauvegarderPartie actualise les données du joueur dans le fichier de sauvegarde
     */
    void sauvegarderPartie(Joueur joueur, String[][] contenuSauvegarde) {
        int idxJoueurSauvegarde = idxStringDansTab(contenuSauvegarde, joueur.nom, IDX_NOM_SAUVEGARDE);
        contenuSauvegarde[idxJoueurSauvegarde][IDX_POSITION_SAUVEGARDE] = "" + joueur.position;
        contenuSauvegarde[idxJoueurSauvegarde][IDX_MEILLEUR_SCORE_SAUVEGARDE] = "" + joueur.meilleurScore;
        contenuSauvegarde[idxJoueurSauvegarde][IDX_SCORE_COURANT_SAUVEGARDE] = "" + joueur.score;
        saveCSV(contenuSauvegarde, CHEMIN_FICHIER_SAUVEGARDE);
    }

    /**
     ****** Fonctions liées au Joueur ******
     */

    /**
     * Cette fonction toString permet d'afficher les données d'une variable de type Joueur
     */
    String toString(Joueur joueur) {
        return  "Joueur : "  + joueur.nom + 
                " - Dernière position en jeu : " + joueur.position + 
                " - Meilleur score du joueur : " + joueur.meilleurScore +
                " - Score courant du joueur : " + joueur.score;
    }

    /**
     * La fonction newJoueur créer une nouvelle variable de type Joueur selon les valeurs données en 
     * paramètre de la fonction
     */
    Joueur newJoueur(String nom, int dernierePosition, int meilleurScore, int score) {
        Joueur joueur = new Joueur();
        joueur.nom = nom;
        joueur.position = dernierePosition;
        joueur.meilleurScore = meilleurScore;
        joueur.score = score;
        return joueur;
    }

    void testNewJoueur() {
        Joueur joueur =  newJoueur("Mathys", 3, 6, 4);
        assertEquals("Mathys", joueur.nom);
        assertEquals(3, joueur.position);
        assertEquals(6, joueur.meilleurScore);
        assertEquals(4, joueur.score);
    }

    /**
     * La fonction affecterJoueur vérifie si un joueur existe dans la sauvegarde. Si il n'existe pas
     * alors le joueur est ajouté à la sauvegarde. Renvoie la variable joueur créee avec les appels à la 
     * fonction newJoueur
     */
    Joueur affecterJoueur(String[][] contenuSauvegarde, String nomJoueur) {
        Joueur joueur;
        int idxJoueurSauvegarde = idxStringDansTab(contenuSauvegarde, nomJoueur, IDX_NOM_SAUVEGARDE);
        if (idxJoueurSauvegarde > 0) {
            joueur = newJoueur( nomJoueur, 
                                stringToInt(contenuSauvegarde[idxJoueurSauvegarde][IDX_POSITION_SAUVEGARDE]), 
                                stringToInt(contenuSauvegarde[idxJoueurSauvegarde][IDX_MEILLEUR_SCORE_SAUVEGARDE]),
                                stringToInt(contenuSauvegarde[idxJoueurSauvegarde][IDX_SCORE_COURANT_SAUVEGARDE]));
        } else {
            joueur = newJoueur(nomJoueur, 0, -1, 0);
            ajouterJoueurASauvegarde(joueur, contenuSauvegarde);
        }
        return joueur;
    }

    /**
     * La fonction deplacerJoueur modifie la variable position du joueur
     */
    void deplacerJoueur(Joueur joueur, int valeur) {
        if ((joueur.position + valeur) < NOMBRE_CASE_PLATEAU) {
            joueur.position = joueur.position + valeur;
        } else {
            joueur.position = 2 * (NOMBRE_CASE_PLATEAU - 1) - valeur - joueur.position;
        }
    }

    /**
     ****** Fonctions liées aux questions ******
     */

    /**
     * Cette fonction toString permet d'afficher les données d'une variable de type Question
     */
    String toString(Question question) {
        return "Question n° : " + question.num + " - Matière : " + question.matiere + " - Intitulé : " + question.intitule + " - Réponse : " + question.reponse + " - Mots clés : " + toString(question.motsClesReponse);
    }

    /**
     * La fonction newQuestion créer une nouvelle variable de type Question selon les valeurs données en 
     * paramètre de la fonction
     */
    Question newQuestion(int num, String matiere, String intitule, String reponse, String[][] motsCles) {
        Question question = new Question();
        question.num = num;
        question.matiere = matiere;
        question.intitule = intitule;
        question.reponse = reponse;
        question.motsClesReponse = motsCles;
        return question;
    }

    String[][] recupererMotsClesReponse(String[][] contenuQuestions, int indiceQuestion) {
        String chaineMotsCles = contenuQuestions[indiceQuestion][3];
        String[] listeChaineMotsCles = decouperChaine(chaineMotsCles, '|');

        final int NOMBRE_MOTS_CLE_MAX = 3;
        String[][] listeMotsCles = new String[length(listeChaineMotsCles)][NOMBRE_MOTS_CLE_MAX];
        for (int idxListeMotsCles = 0; idxListeMotsCles < length(listeMotsCles); idxListeMotsCles++) {
            listeMotsCles[idxListeMotsCles] = decouperChaine(listeChaineMotsCles[idxListeMotsCles], ';');
        }

        return listeMotsCles;
    }

    /**
     * La fonction obtenirQuestionMatiere récupère les données du fichier des questions d'une matière donnée et retourne
     * une variable une variable de type Question contenant tout les élements de la question récupérés
     * dans les fichier
     */
    Question obtenirQuestionMatiere(String matiere) {
        final String CHEMIN_DOSSIER_QUESTIONS = "../questions/questions";
        String[][] contenuQuestions = recupererContenuCSV(CHEMIN_DOSSIER_QUESTIONS + matiere + ".csv");
        int nombreQuestions = length(contenuQuestions) - 1;
        int indiceQuestion = entierRandom(1, nombreQuestions + 1);
        String[][] motsCles = recupererMotsClesReponse(contenuQuestions, indiceQuestion);
        return newQuestion( indiceQuestion, 
                            contenuQuestions[indiceQuestion][0], 
                            contenuQuestions[indiceQuestion][1], 
                            contenuQuestions[indiceQuestion][2], 
                            motsCles);
    }

    /**
     * La fonction obtenirMatiereCase renvoie la matière associé à l'index d'une case. Si l'index ne 
     * correspond à aucune matière alors une chaine de caractère vide est renvoyée
     */
    String obtenirMatiereCase(int indexCase, int[] themesCases) {
        String matiere = "";
        if (indexCase >= length(themesCases)) {
            return matiere;
        }
        int numThemeCase = themesCases[indexCase];
        if (numThemeCase < 0 || numThemeCase > 4) {
            return matiere;
        }
        if (numThemeCase == 0) {
            matiere = "Anglais";
        } else if (numThemeCase == 1) {
            matiere = "ChiffresRomains";
        } else if (numThemeCase == 2) {
            matiere = "Francais";
        } else if (numThemeCase == 3) {
            matiere = "Geographie";
        } else if (numThemeCase == 4) {
            matiere = "Histoire";
        }
        return matiere;
    }

    void testObtenirMatiereCase() {
        final int[] THEMES_CASES = new int[]{0,2,4,3,1};
        assertEquals("Anglais", obtenirMatiereCase(0, THEMES_CASES));
        assertEquals("ChiffresRomains", obtenirMatiereCase(4, THEMES_CASES));
        assertEquals("", obtenirMatiereCase(5, THEMES_CASES));
    }

    /**
     * La fonction renommerMatiereQuestion modifie la chaine de caractère contenant le thème de la 
     * question pour que le thème puisse être affiché de manière accordée avec le reste de la phrase
     */
    void renommerMatiereQuestion(Question question) {
        if (equals(question.matiere, "ChiffresRomains")) {
            question.matiere = "sur les chiffres romains";
        } else if (equals(question.matiere, "Anglais")) {
            question.matiere = "d'anglais";
        } else if (equals(question.matiere, "Francais")) {
            question.matiere = "de francais";
        } else if (equals(question.matiere, "Geographie")) {
            question.matiere = "de geographie";
        } else if (equals(question.matiere, "Histoire")) {
            question.matiere = "d'histoire";
        }
    }

    boolean estBonneReponse(Question question, String chaineReponse) {
        chaineReponse = toUpperCase(chaineReponse);
        String[][] listeMotsCles = question.motsClesReponse;
        
        boolean bonneReponse = false;
        boolean contenu = true;
        int indexListeMotsCles = 0;
        int indexMotsCles = 0;

        while (!bonneReponse && indexListeMotsCles < length(listeMotsCles)) {
            toUpperCase(listeMotsCles[indexListeMotsCles]);
            while (contenu && indexMotsCles < length(listeMotsCles[indexListeMotsCles])) {
                contenu = chaineEstContenue(listeMotsCles[indexListeMotsCles][indexMotsCles], chaineReponse);
                indexMotsCles += 1; 
            }
            if (contenu) {
                bonneReponse = true;
            } else {
                contenu = true;
                indexListeMotsCles += 1;
                indexMotsCles = 0;
            }
        }

        return bonneReponse;
    }

    /**
     ****** Fonctions de vérification de la saisie utilisateur ******
     */

    /**
     * La fonction estPrenomValide vérifie que le nom du joueur n'est pas trop long, qu'il ne contient 
     * pas de retours à la ligne ni de virgules pour qu'il ne soit pas considérer comme un séparateur 
     * dans le fichier CSV
     */
    boolean estPrenomValide(String chaine) {
        return !(length(chaine) <= 0 || length(chaine) > 20 || charEstDansString(chaine, ',') || charEstDansString(chaine, '\n'));
    }

    /**
     ****** Fonctions de gestion du plateau de jeu ******
     */

    /**
     * La fonction genererThemesCasescgénére un tableau d'entiers contenant les indexs des différents thèmes des questions déterminés
     * de manière aléatoire
     * 0 = Anglais
     * 1 = Chiffres romains
     * 2 = Français
     * 3 = Géographie
     * 4 = Histoire
     */
    int[] genererThemesCase() {
        int[] themes = new int[NOMBRE_CASE_PLATEAU - 2];
        for (int idxCase = 0; idxCase < length(themes); idxCase++) {
            themes[idxCase] = entierRandom(0, 5);
        }
        return themes;
    }

    /**
     * La fonction genererCasesPlateau renvoie un tableau à deux dimensions contenant chaque ligne, et 
     * pour chaque ligne chaque colonne, du plateau. Les patterns des cases sont stockés au format CSV et 
     * il existe différents patterns pour une même case permettant à chacune de s'imbriquer. Par exemple, 
     * certaines cases ne possèdent pas de bords gauche afin qu'elle puisse s'imbriquer avec une case à 
     * sa gauche. Ces cases sont celles aux lignes 7 à 11 du fichier CSV. Celles ouvertes sur la droite 
     * sont celles au ligne 12 à 16 du fichier CSV. La fonction détermine donc quel pattern utilisé pour 
     * une case en fonction de la position du joueur et du thème de la case.
     */
    String[][] genererCasesPlateau(int positionJoueur, int[] themesCases) {
        /**
         * Indices des cases thèmes ouvertes sur la gauche dans le tableau de patterns
         *      7 --> 11
         * Indices des cases thèmes ouvertes sur la droite dans le tableau de patterns
         *      12 --> 16
         */
        final int IDX_CASE_DEPART = 0;
        final int IDX_CASE_FIN = 1;
        final int IDX_CASE_JOUEUR = 20;
        final int IDX_CASE_JOUEUR_GAUCHE = 21;
        final int IDX_CASE_JOUEUR_DROITE = 22;

        String[][] indicesPlateau = new String[NOMBRE_CASE_PLATEAU][7];
        String[][] patternsPlateau = recupererContenuCSV("../patterns/cases_pattern.csv");

        indicesPlateau[0] = patternsPlateau[IDX_CASE_DEPART];
        indicesPlateau[NOMBRE_CASE_PLATEAU - 1] = patternsPlateau[IDX_CASE_FIN];
        if (positionJoueur == (NOMBRE_CASE_PLATEAU - 1)) {
            indicesPlateau[positionJoueur] = patternsPlateau[IDX_CASE_JOUEUR_GAUCHE];
        } else if (positionJoueur == 0) {
            indicesPlateau[positionJoueur] = patternsPlateau[IDX_CASE_JOUEUR_DROITE];
        } else {
            indicesPlateau[positionJoueur] = patternsPlateau[IDX_CASE_JOUEUR];
        }
        for (int idxCase = 1; idxCase < positionJoueur; idxCase++) {
            indicesPlateau[idxCase] = patternsPlateau[themesCases[idxCase - 1] + 7];
        }
        for (int idxCase = positionJoueur + 1; idxCase < (NOMBRE_CASE_PLATEAU - 1); idxCase++) {
            indicesPlateau[idxCase] = patternsPlateau[themesCases[idxCase - 1] + 12];
        }

        return indicesPlateau;
    }

    /**
     * La fonction assemblerPlateau renvoie une chaine de caractères fabriquées selon les patterns 
     * donnés par la fonction genererCasesPlateau. La plateau est assemblée ligne par ligne. Cette 
     * solution est une solution simple pour imbriquer les tableaux sans être embêtés par les retours à 
     * la ligne et pouvoir faire simplement la colorisation de ce dernier
     */
    String assemblerPlateau(String[][] casesPlateau) {
        String plateau = "";
        for (int idxLigne = 0; idxLigne < length(casesPlateau[0]); idxLigne++) {
            for (int idxCase = 0; idxCase < length(casesPlateau); idxCase++) {
                if (idxCase == 0 || idxCase == (length(casesPlateau) - 1)) {
                    plateau = plateau + utiliserCouleur("bleu") + casesPlateau[idxCase][idxLigne] + utiliserCouleur("reset");
                } else if (idxCase == 1) {
                    String ligne = casesPlateau[idxCase][idxLigne];
                    plateau = plateau + utiliserCouleur("bleu") + charAt(ligne, 0) + utiliserCouleur("reset") + substring(ligne, 1, length(ligne)); 
                } else {
                    plateau = plateau + casesPlateau[idxCase][idxLigne];
                }
            }
            plateau = plateau + '\n';
        }
        plateau = supprimerCaractereIdx(length(plateau) - 1, plateau);
        return plateau;
    }

    /**
     ****** Fonctions utiles ******
     */

    /**
     * Cette fonction toString s'applique aux tableaux de chaines de caractères à une dimension
     */
    String toString(String[] tab) {
        if (length(tab) <= 0) {
            return "";
        } else {
            String chaine = "";
            for (int idxTab = 0; idxTab < length(tab) - 1; idxTab++) {
                chaine = chaine + tab[idxTab] + " - ";
            }
            chaine = chaine + tab[length(tab) - 1];
            return chaine;
        }
    }

    /**
     * Cette fonction toString s'applique aux tableaux de chaines de caractères à deux dimensions
     */
    String toString(String[][] tab) {
        String chaine = "";
        for (int idxLigne = 0; idxLigne < length(tab); idxLigne++) {
            for (int idxColonne = 0; idxColonne < length(tab[idxLigne]); idxColonne++) {
                if (idxColonne == (length(tab[idxLigne]) - 1)) {
                    chaine = chaine + tab[idxLigne][idxColonne] + '\n';
                } else {
                    chaine = chaine + tab[idxLigne][idxColonne] + " - ";
                }
            }
        }
        return chaine;
    }

    /**
     * Cette fonction toString s'applique aux tableaux d'entiers à une dimension
     */
    String toString(int[] tab) {
        if (length(tab) <= 0) {
            return "";
        } else {
            String chaine = "";
            for (int idxTab = 0; idxTab < (length(tab) - 1); idxTab++) {
                chaine = chaine + tab[idxTab] + " - ";
            }
            chaine = chaine + tab[length(tab) - 1];
            return chaine;
        }
    }

    /**
     * La fonction idxStringDansTab renvoie l'indice de la première occurence d'une chaine de caractères 
     * dans un tableau à deux dimensions pour une colonne donnée. Si la chaine n'existe pas, la fonction 
     * renvoie -1.
     */
    int idxStringDansTab(String[][] tab, String chaine, int idxColonneCiblee) {
        int indice = -1;
        int idxLigne = 0;
        boolean trouve = false;
        chaine = toUpperCase(chaine);

        while (!trouve && idxLigne < length(tab)) {
            if (equals(toUpperCase(tab[idxLigne][idxColonneCiblee]), chaine)) {
                trouve = true;
                indice = idxLigne;
            } else {
                idxLigne = idxLigne + 1;
            }
        }

        return indice;
    }

    /**
     * La fonction charEstDansString renvoie true si le caractère donné est contenu dans la chaine de 
     * caractères donnée
     */
    boolean charEstDansString(String chaine, char car) {
        boolean trouve = false;
        int idxChaine = 0;
        while (!trouve && (idxChaine < length(chaine))) {
            if (car == charAt(chaine, idxChaine)) {
                trouve = true;
            }
            idxChaine = idxChaine + 1;
        }
        return trouve;
    }

    void testCharEstDansString() {
        assertTrue(charEstDansString("bonjour", 'b'));
        assertFalse(charEstDansString("test", 'a'));
    }

    /**
     * La fonction lireFichier reçoit en paramètre le chemin relatif d'un fichier et renvoie son contenu 
     * sous la forme d'une chaine de caractères
     */
    String lireFichier(String cheminFichier) {
        File fichierElements = newFile(cheminFichier);
        String chaine = "";
        while (ready(fichierElements)) {
            chaine = chaine + readLine(fichierElements) + '\n';
        }
        return chaine;
    }

    /**
     * La fonction entierRandom renvoie un entier compris dans l'intervalle borneGauche inclus à 
     * borneDroite exclus.
     * 
     * Attention : borneDroite doit être supérieur ou égal à borneGauche sinon 0 est retourné.
     */
    int entierRandom(int borneGauche, int borneDroite) {
        if (borneDroite < borneGauche) {
            return 0;
        }

        double alea = random();
        return (int)(alea * (borneDroite - borneGauche) + borneGauche);
    }

    /**
     * La fonction supprimerCaractereIdx supprime le caractère d'indice n (donné en paramètre) d'une 
     * chaine de caractères
     */
    String supprimerCaractereIdx(int idxASupprimer, String chaine) {
        String chaineModifiee = "";
        for (int idxChaine = 0; idxChaine < length(chaine); idxChaine ++) {
            if (idxChaine != idxASupprimer) {
                chaineModifiee = chaineModifiee + charAt(chaine, idxChaine);
            }
        }
        return chaineModifiee;
    }

    int compterOccurencesCaractereDansChaine(char car, String chaine) {
        int cptOccurences = 0;
        for (int idxChaine = 0; idxChaine < length(chaine); idxChaine++) {
            if (charAt(chaine, idxChaine) == car) {
                cptOccurences += 1;
            }
        }
        return cptOccurences;
    }

    int[] trouverIndicesCaractereDansChaine(char car, String chaine) {
        int nbOccurences = compterOccurencesCaractereDansChaine(car, chaine);
        int[] indicesOccurences = new int[nbOccurences];
        int nbOccurencesTrouvees = 0;
        int cpt = 0;
        while ((cpt < length(chaine)) && (nbOccurencesTrouvees < nbOccurences)) {
            if (charAt(chaine, cpt) == car) {
                indicesOccurences[nbOccurencesTrouvees] = cpt;
                nbOccurencesTrouvees += 1;
            }
            cpt += 1;
        }
        return indicesOccurences;
    }

    String[] decouperChaine(String chaine, char separateur) {
        int[] indicesDecoupage = trouverIndicesCaractereDansChaine(separateur, chaine);
        int nombreChaines = length(indicesDecoupage) + 1;
        String[] listeChaines = new String[nombreChaines];
        int indiceCaractereDebut = 0;
        for (int cptCoupage = 0; cptCoupage < (nombreChaines - 1); cptCoupage++) {
            listeChaines[cptCoupage] = substring(chaine, indiceCaractereDebut, indicesDecoupage[cptCoupage]);
            indiceCaractereDebut = indicesDecoupage[cptCoupage] + 1;
        }
        listeChaines[nombreChaines - 1] = substring(chaine, indiceCaractereDebut, length(chaine));
        return listeChaines;
    }

    boolean chaineEstContenue(String chaineContenue, String chaineContenant) {
        int idxChaine = 0;
        boolean trouve = false;
        int longueurAVerifier = length(chaineContenant) - length(chaineContenue);

        while (!trouve && idxChaine <= longueurAVerifier) {
            if (charAt(chaineContenant, idxChaine) == charAt(chaineContenue, 0)) {
                trouve = equals(chaineContenue, substring(chaineContenant, idxChaine, idxChaine + length(chaineContenue)));
            }
            idxChaine += 1;
        }

        return trouve;
    }

    void testChaineEstContenue() {
        assertTrue(chaineEstContenue("ouge", "rouge"));
        assertTrue(chaineEstContenue("roug", "rouge"));
        assertFalse(chaineEstContenue("mot", "non compris"));
        assertTrue(chaineEstContenue("bonjour", "Il te passe le bonjour, tu comprends ?"));
        assertTrue(chaineEstContenue("Khéops", "Les jdhhf de Khéops"));
    }

    void toUpperCase(String[] tab) {
        for (int idxTab = 0; idxTab < length(tab); idxTab++) {
            tab[idxTab] = toUpperCase(tab[idxTab]);
        }
    }

    void testToUpperCase() {
        String[] tableauMinuscule = new String[]{"bonjour", "ceci", "est", "un", "test", "accentuééééé"};
        String[] tableauMajuscule = new String[]{"BONJOUR", "CECI", "EST", "UN", "TEST", "ACCENTUÉÉÉÉÉ"};

        toUpperCase(tableauMinuscule);
        assertArrayEquals(tableauMinuscule, tableauMajuscule);
    }

}