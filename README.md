# PLE 2026 — Pengo

## Présentation

Ce dépôt contient le rendu du projet PLE : un moteur de jeu 2D générique et un jeu rétro de type **Pengo** construit avec ce moteur.

Le projet respecte l'organisation demandée autour d'une architecture **MVCB** :

- **Model** : monde simulé, grille, entités, géométrie, tore, collisions, déplacements et stunts ;
- **View** : affichage, avatars, HUD, viewport, animations, debug et mesures de performance ;
- **Controller** : gestion des entrées clavier/souris ;
- **Brain / Bots** : comportements automatiques des entités, avec des automates GAL chargés au démarrage.

Le jeu utilise des cartes textuelles, des ressources graphiques PNG et des fichiers `.gal` pour définir certains comportements.

## Contenu du dépôt

```text
.
├── engine/                         # Source folder Eclipse du moteur
│   └── engine/
│       ├── model/                  # Géométrie, grille, modèle, entités, collisions, stunts
│       ├── brain/                  # Bots génériques, PlayerBot, Brain
│       ├── controller/             # Contrôleurs génériques clavier/souris
│       └── view/                   # Vue, avatars, painter, ticker, viewport, HUD/debug
├── pengo/                          # Source folder Eclipse du jeu
│   └── game/
│       ├── MainPengo.java          # Classe principale à lancer
│       ├── BuildMap.java           # Chargement des cartes textuelles
│       ├── brain/                  # Bots spécifiques au jeu
│       ├── config/                 # Configuration globale du jeu
│       │   ├── map/                # Lecture et choix des cartes
│       │   ├── menu/               # Menu principal, pause, fin de partie
│       │   ├── session/            # Création et cycle de vie d'une partie
│       │   └── spawn/              # Installation des bots et spawners
│       ├── controller/             # Contrôleur de Pengo
│       ├── gal/                    # Adaptation entre GAL et le jeu
│       ├── model/
│       │   ├── entity/             # Enemies, Glacon, Mur, Power, Spawner...
│       │   ├── player/             # Pengo, Friend, Player
│       │   └── slide/              # Glissade, poussée, friction, collisions de glissade
│       └── view/                   # Avatars et overlay de Pengo
├── gal/                            # Parser et AST du langage GAL
│   └── gal/
│       ├── ast/
│       ├── parser/
│       └── util/
├── tests/                          # Tests JUnit du moteur
├── pengo_maps/                     # Cartes textuelles du jeu
├── pengo_rsrc/                     # Images et sprites du jeu
├── EnemyChase.gal                  # Automate des ennemis
├── FriendRandom.gal                # Automate du Friend
├── SpawnerLoadTest.gal             # Automate pour le test de charge des spawners
├── given.jar                       # Bibliothèque graphique/tâches fournie
├── CONTRAT.md                      # Contrat de projet
├── PARTICIPATION.md                # Implication des membres de l'équipe
└── README.md                       # Ce fichier
└── video_pengo.mp4                 # video presantant le jeu
```

Remarque : dans Eclipse, `engine`, `pengo`, `gal` et `tests` sont des **source folders**. C'est pour cela que les packages visibles sont `engine.model`, `engine.view`, `game.model.entity`, `game.config.map`, etc. Le package principal du jeu est donc `game`, même si les fichiers sont rangés dans le dossier `pengo/`.

## Prérequis

- Java avec un JDK récent.
- JavaCC pour régénérer le parser GAL depuis `parser.jj`.
- Le fichier `given.jar`, présent à la racine du projet.
- Pour les tests : JUnit configuré dans Eclipse ou disponible dans le classpath.

Le projet doit être compilé et lancé depuis la **racine du dépôt**, car les chemins des cartes, images et fichiers GAL sont relatifs à cette racine.

## Compiler en ligne de commande

Depuis la racine du dépôt :

```bash
mkdir -p bin
find bin -name "*.class" -delete

# 1. Régénérer les fichiers Java du parser GAL avec JavaCC
cd gal/gal/parser
javacc parser.jj
cd ../../..

# 2. Compiler le moteur, le jeu Pengo et le parser GAL
find engine pengo gal -name "*.java" > sources.txt
javac -encoding UTF-8 -cp given.jar -d bin @sources.txt
rm sources.txt
```

Le dossier `bin/` n'est pas versionné dans Git : il est généré localement à la compilation.

La commande `javacc parser.jj` génère les fichiers Java du parser GAL à partir de la grammaire `gal/gal/parser/parser.jj`. Ensuite, la commande `javac` compile le moteur, le jeu Pengo et tous les fichiers du parser.

## Lancer le jeu en ligne de commande

Depuis la racine du dépôt, après compilation :

```bash
java -ea -cp "bin:given.jar" game.MainPengo
```

Sous Windows, remplacer `:` par `;` dans le classpath :

```bash
java -ea -cp "bin;given.jar" game.MainPengo
```

L'option `-ea` active les assertions Java. Elle est recommandée, car le moteur utilise des assertions pour vérifier les invariants de configuration, géométrie, grille, collisions et déplacements.

## Compiler et lancer avec Eclipse

Même avec Eclipse, il faut d'abord régénérer les fichiers Java du parser GAL à partir de `parser.jj`.

Depuis la racine du dépôt :

```bash
cd gal/gal/parser
javacc parser.jj
cd ../../..
```

Ensuite :

1. Ouvrir Eclipse.
2. Importer le projet avec `File > Import > Existing Projects into Workspace`.
3. Sélectionner le dossier racine du dépôt.
4. Vérifier que les source folders suivants existent dans le Build Path :
   - `engine`
   - `pengo`
   - `gal`
   - `tests`
5. Vérifier que `given.jar` est bien dans le Build Path.
6. Faire `Project > Clean`, puis laisser Eclipse recompiler le projet.
7. Lancer la classe :

```text
game.MainPengo
```

## Lancer les tests

Les tests JUnit sont dans le dossier `tests/`.

Dans Eclipse, lancer la suite complète :

```text
junit.suites.SuiteMoteurComplet
```

Des suites plus ciblées existent aussi :

```text
junit.suites.SuiteMoteurModel
junit.suites.SuiteMoteurBrain
junit.suites.SuiteMoteurController
junit.suites.SuiteMoteurView
```

Ces tests vérifient les parties génériques du moteur : axes, tore, grille, ISU, entités, formes, collisions, stunts, bots, contrôleurs et vue.

## Principe du jeu

Le jeu est une adaptation rétro de **Pengo**.

Le joueur contrôle Pengo dans un labyrinthe de glace. Il doit survivre, utiliser les blocs de glace, exploiter les pouvoirs et éliminer les ennemis. La partie est gagnée lorsque tous les ennemis attendus ont été éliminés. Elle est perdue lorsque Pengo n'a plus de vies.

Fonctionnalités principales :

- déplacement case par case ;
- ennemis pilotés par automates GAL ;
- Friend activable et contrôlable ;
- blocs de glace poussables ;
- glissade avec friction ;
- spawners capables de générer des ennemis ;
- collisions entre joueurs, ennemis, murs, blocs de glace, spawners et pouvoirs ;
- pouvoirs : bouclier, vitesse, freeze, étoile, vie supplémentaire, BoomFish, activateur Friend ;
- HUD avec vies, score, pouvoirs actifs et état de la partie ;
- cartes toriques ou non toriques ;
- viewport pour suivre le joueur sur certaines cartes ;
- options de debug et de test de charge.
- menu principal, menu pause et écran de fin de partie ;

## Commandes du jeu

### Menu principal

- `Flèche haut` / `Flèche bas` : changer de carte.
- `Entrée` : lancer la carte sélectionnée.
- `Échap` ou `Q` : quitter.
- Souris : sélectionner une carte ou cliquer sur le bouton de lancement.

### En jeu

- `Z`, `Q`, `S`, `D` ou flèches directionnelles : déplacer Pengo.
- `Espace` : utiliser l'action devant le joueur, notamment pousser un bloc de glace ou un spawner.
- `Tab` : changer de personnage contrôlé lorsque le Friend est disponible.
- `Échap` : pause.
- Clic gauche : orienter le personnage contrôlé.
- Clic droit : utiliser l'action devant le personnage contrôlé.

### Menu pause

- `Entrée` : reprendre la partie.
- `M` : revenir au menu principal.
- `Q` : quitter.

### Debug

- `F1` : activer/désactiver l'affichage debug.
- `F2` : afficher/masquer les boîtes de collision.
- `F3` : afficher/masquer les actions courantes.
- `F4` : afficher/masquer les informations de performance.
- `F5` : activer/désactiver le mode de test de charge.

## Cartes

Les cartes sont dans :

```text
pengo_maps/
```

Elles sont chargées automatiquement par le menu principal.

Symboles utilisés dans les cartes :

```text
#  mur
.  case libre
I  bloc de glace
P  Pengo
S  spawner
E  ennemi
F  Friend
1  bouclier
2  vitesse
3  freeze
4  étoile
5  vie supplémentaire
6  BoomFish
A  activateur Friend
```

Une carte peut être torique lorsqu'elle possède des ouvertures sur ses bords. Les cartes toriques permettent de montrer la gestion du tore et du viewport.

## Automates GAL

Les fichiers GAL utilisés par le jeu sont à la racine du dépôt :

```text
EnemyChase.gal
FriendRandom.gal
SpawnerLoadTest.gal
```

Ils sont chargés au démarrage d'une session de jeu. Modifier ces fichiers permet de changer certains comportements des bots sans modifier le code Java.

## Ressources graphiques

Les images du jeu sont dans :

```text
pengo_rsrc/
```

Elles sont utilisées pour les avatars de Pengo, du Friend, des ennemis, des blocs de glace, des murs, des pouvoirs, des spawners, du menu et de l'overlay.
