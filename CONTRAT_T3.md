# CONTRAT

* Équipe 3 
* Membres : FORTIER Gregoire , EL MEOUCH Lynn, MAJED Houssein , HAJJ DIAB Mira , BARROUG Abdellah
* Contact : Lynn.El-Meouch@etu.univ-grenoble-alpes.fr
* Jeu : Pengo
* version relue et amendé par M.Périn


# 1. Point technique à expliquer durant la démo

* [x] phsyique des glaçons et gestion de la collision d'un bloc glissant avec une ennemi et le transfert de quantité de mouvement 

### Physiques de glaçons

J'attire votre attention sur le fait qu'il y a plusieurs choix à faire concernant la physique des glaçons :
- soit vitesse constante et pas de friction
- soit vitesse qui décroit pour s'arrêter sur un bon emplacement

Votre contrat mentionne deux fois la physique des glaçons ; je les ai rassemblés ici:  

* [x]**Physique Galçons** :
  - soit ralentissement jusqu'à vitesse min, jusqu'à que le bloc soit sur un bon emplacement puis v=0
  - [#MP: que se passe t'il quand un bloc + un ennemi sont collés et en glissade?]

* [x] __physique & collisions spécifiques__    
  + Explications: 
    - Un bloc poussé avance automatiquement dans une direction jusqu'à rencontrer un obstacle.
    + [#MP: et si le bloc ne rencontre aucun obstacle dans un monde torique ?]
  + Réalisation technique :
    + [on associe un vecteur vitesse constant au bloc et le monde n'a pas de friction donc il reste constant]
    + [en cas de collision, le bloc transmet (ou non) son vecteur vitesse à l'entité impacté selon la nature de l'entité : bloc, ennemi, joueur,...] 

  + **Démo** 
    - Pousser un bloc de glace, il glisse jusqu'à rencontrer obstacle 
    - S'il rencontre un ennemi il le pousse jusqu'à ce que groupe bloc + ennemi rencontre un obstacle
    - Si le groupe bloc + ennemi ne rencontre pas d'obstacle,       
      - [o] ils tournent indéfiniment dans le tore 
      - [o] leur vitesse décroient et ils s'arrêtent sur un « bon » emplacement


# 2. Contrat lié au moteur de jeu

## Partie Modèle

### Géometrie

* [x] __monde torique__
  - Explications:
    - même géométrie pour  le joueur, les ennemis et les glaçons. 
    - Si un glaçon mobile glisse sans rencontrer d’obstacle, sa vitesse diminue progressivement jusqu’à l’arrêt.
  - **Démo : Montrer le joueur, un ennemi ou un glaçon mobile qui sort par un bord de la carte et réapparaît du côté opposé.**


* [ ] __défilement / scrolling__
 
* [?] __taille de la map modifiable ?__
  - Explications: En cas de niveau supérieur

* [ ] __bords du monde__ 
  - Explications:  
    - Pas de bords. Le monde est torique.    
    - Certains glaçons fixes, de couleur plus claire, servent d’obstacles et ne se déplacent pas.

### Génération de la map

* [x] __fixe__ :
  - Explications: 
    - la taille de la map est fixée par configuraiont [#MP: à préciser]
    - elle ne dépend pas de la taille du fichier ASCII

* [x] __par configuration__ :
  - Explications: 
    + [o] Les maps seront décrites dans un fichier ASCII 
      - `#` : mur ;
      - `.` : case vide ;
      - `P` : joueur ;
      - `B` : bloc de glace ;
      - `E` : ennemi ;
      - `C` : cristal.
    + [o] ou dans une structure séparée du code principal.
  - **Démo :  Modifier la configuration d'un niveau, relancer le jeu et montrer que la map a changé.**

* [ ] __aléatoire__

* [ ] __aléatoire avec mémorisation__


### Interactions

* [ ] __annulation d'une action__

* [x] __collisions entre entités__
  - Explications: gérées par le moteur mais des règles de priorité seront définies (cf. interactions spéficiques).
  - **Démo : collision standard** 


* [x] __interactions spécifiques__
  + Explications: 
    1. Un bloc de glace en glissade est prioritaire sur un ennemi et peut l'écraser. 
    2. Le joueur ne peut pas entrer dans une case occupée par un mur ou un bloc immobile (collision standard) 
    3. Entre bloc et ennemi, priorité au bloc [#MP: c'est le cas 1] 
    4. pousse les 2. [#MP: pas clair, à préciser, voir item suivant]
  + **Démo : une séquence de démo pour chacune des 4 situations décrites ci-dessus**



### Affichage et animation 

* [x]  __barre de santé / points de vie de chaque personnage__
  - Explications: Le joueur aura un nombre de vies affiché à l'écran. Les ennemis auront principalement deux états : vivant ou éliminé.
  - **Démo : Montrer la perte d'une vie lorsque le joueur touche un ennemi.**

* [x] __animation lors de la création / apparition__
  - Explications : 
    - Au lancement d'un niveau, les entités apparaissent à leur position initiale : joueur, ennemis, blocs de glace et cristaux.   
    - Une animation simple d'apparition pourra être utilisée.
  + **Démo : Lancer un niveau et montrer [l'animation] d'apparition des entités.**

* [x] __animation lors de la destruction / disparition__
  - Explications : 
    - Lorsqu'un cristal est récupéré, il disparaît [#MP: est-ce une animation?]. 
    - Lorsqu'un ennemi est écrasé par un bloc de glace en glissade, il disparaît avec une animation simple.
  + **Démo : pousser un bloc sur un ennemi pour voir l'animation «ennemi écrasé».**

* [x] __animation en fonction de l'état du Bot (Waiting, Resting, ...)__
  - Explications : Les ennemis pourront changer d'animation selon leur état : attente, déplacement, poursuite, fuite ou élimination.
  + **Démo** 
    + Afficher un ennemi qui change [d'aspect] quand il change [d'état `Searching` =] déplacement aléatoire vers []`Targeting` =] une poursuite du joueur.
    + [affichage de l'état du bot en mode debug pour qu'on voit que le changement d'aspect correspond à l'état du bot]


* [ ] __animations / affichage spécifiques__
   - Explications : 
    - L'animation principale spécifique au jeu sera la glissade des blocs de glace. 
    - [#MP: ce n'est pas une animation par changement d'image c'est géré par la physique du moteur, voir section «physique et collisions spécifiques»]
    

#### Adaptabilité = changement pendant le jeu

* [?] changement de Stunt 
  - Explications = changement de façon de faire une action : 
    - Le moteur permettra de changer la manière d'effectuer certaines actions. 
    - Par exemple, l'action `push` pourra être remplacée par une poussée spéciale ou une poussée plus rapide.
  - Démo: 
    - pousser un bloc normalement, 
    - puis passer à une variante où le bloc glisse plus vite ou avec un effet différent.

* [ ] changement d'avatar  [#MP: ce que vous décrivez n'est pas un changement d'avatar]
  + Explications:
    _Avoir plusieurs images/animations pour des actions différentes ne constitue pas un changement d'avatar. Un changement d'Avatar est nécessaire lorsque les images/les animations associée aux actions (eg. Move, Hit) changent, par exemple lorsque le personnage a récupéré une arme, un costume, un bonus qui change profondément son aspect._ 
    - L'image du joueur ou des ennemis changera selon la direction ou l'état. 
    - Par exemple, le joueur peut avoir une image différente selon qu'il regarde vers le haut, le bas, la gauche ou la droite.
 
  - Démo : changement d'image : 
    - déplacer le joueur dans plusieurs directions et montrer le changement d'avatar. 
    - Montrer aussi un ennemi avec une image différente lorsqu'il est éliminé.

* [x] changement dynamique de bot/FSM 
  - Explications : 
    - _Par exemple, Les ennemis se comportement de manière peu agressive quand ils sont nombreux et au cours de la partie, dans le même niveau, ils changent pour un automate plus agressif quand ils sont moins nombreux._
    - Les ennemis pourront changer de comportement pendant le jeu. 
    - Par exemple, un ennemi pourra passer d'un comportement à un comportement de poursuite.
    - Remplacer ou modifier le Bot/FSM d'un ennemi pendant l'exécution.
  - **Démo** 
     + en mode debug pour qu'on voit l'état de l'automate du bot
     + les ennemis changent d'automate quand ils sont moins nombreux pour un automate plus aggressif

      
## Partie Vue

* [x] view port centré sur le joueur
  + **Démo : [#MP: pour le voir cela suppose que la map est plus grand que le viewport et qu'on ne voit pas toute la map]**

* [ ] zoom
* [ ] scrolling
  

## Mode debug

* [x] affichage de l'action au dessus du personnage
* [x] affichage des bounding box    
  
### performances

* [x] affichage du temps entre deux _tick_ (min, max, moyenne)
* [x] affichage du temps entre deux _paint_ (min, max, moyenne)
* [x] affichage du nombre de _FPS_ (min, max, moyenne)

* [x] gestion de la charge 
  - Explication: _création d'un bot spawner avec l'automate (1)--EGG-> (1) qui génère sans fin de nouvelles entités. Ce qui permet de voir à partir de combien d'entités le moteur tombe à < 24 fps._
  - **Démo: spawner sans limite + affichage du nombre de FPS**


## Partie `Controller` = player

### Player : Action unique _versus_ actions multiples simultanées 

* [o] action unique
  - Explications: une action ne peut avoir lieu que lorsque la précédente est terminée
  - **Démo: le joueur avance ou pousse mais pas les deux en simultanée**

* [ ] mémorisation des actions demandées
  - Explication: 
    - on mémorise les actions tapées au clavier
    - elles peuvent être jouées dans l'ordre ou bien constituer des «combo»
    - elles se périment au cours du temps
  - Démo:

* [o] actions multiples indépendantes pour le joueur
  - Explications:
    - [#MP: il me semble que le joueur peut pousser et se déplacer simultanément]  
  + **Démo: déplacement et action de pousser simultanées**

* [ ] gestion des actions multiples compatibles/incompatibles
  - Explications: _Par exemple, double saut ou bien pendant un saut le joueur peut se diriger à droite ou à gauche_
  - Réalisation technique:
  - Démo:

## Partie Bot

  * [ ] Bot en Java
  * [x] Bot en FSM Java
  * [?] Bot en GAL - parser
  * [ ] Démo : modification du comportement des entités 
      * [ ] en changeant de classe Bot 
      * [x] en changeant de FSM Java
      * [?] en changeant le fichier `.gal` 

### Bot : Action unique _versus_ actions multiples simultanées 

* [x] une action à la fois pour les bots 
  - Explications: _déplacement ou pousser_
  - Démo: _comportement en boucle de déplacement puis pousser_

* [ ] actions multiples indépendantes pour les bots
  - Explications: _déplacement et tir_
  - Réalisation technique:
  - Démo: _déplacement et tir simultanés_


# 3. Contrat spécificique au jeu choisi

## 1. Description brève du jeu

Notre projet est un jeu inspiré de Pengo.

Le joueur contrôle un personnage dans un labyrinthe de glace vu du dessus. Le but de posser des blocs de glace por tuer les ennemis en les evitant.

Pour se défendre, le joueur peut pousser des blocs de glace. Lorsqu'un bloc est poussé, il glisse en ligne droite jusqu'à rencontrer un obstacle. Si un bloc en glissade touche un ennemi, l'ennemi est éliminé.

Le jeu mélange donc :

- déplacement dans un labyrinthe ;
- récupération d'items ;
- ennemis à éviter ;
- interaction avec le décor ;
- stratégie avec les blocs de glace.

Le niveau est gagné lorsque tous les ennemis sont dead. Le joueur perd s'il est touché par un ennemi et qu'il n'a plus de vies.

### 2. Extensions possibles

~~Si la version principale est terminée~~, nous pourrons ajouter plusieurs extensions :

[#MP: si c'est coché ce n'est pas un «Si» c'est dans le contrat]

[x] plusieurs types d'ennemis [#MP: c'est promis dans la section suivante]

[x] des ennemis qui poursuivent le joueur 
  - Réalisation technique : implémentation de la condition Closest

[ ] un timer  

Planning de développement : Ces extensions ne seront ajoutées qu'après avoir obtenu une version minimale complète et stable.

## 3. Difficultés techniques et solutions envisagées


[x]**Comportement des ennemis** 
  - Les ennemis ont d’abord un comportement simple. 
  - Quand il reste peu d’ennemis, ils deviennent enragés : ils vont plus vite et sont plus dangereux.
  + **Démo : deux sortes d'ennemis décrits par deux automates différents**


[?] **FriendBot** 
  - Le joueur possède [un double]. 
  - Quand l’utilisateur appuie sur la touche `S`, il échange le contrôle avec cet ami. 
  - Le personnage contrôlé devient le l'ami, et l’ancien joueur devient un FriendBot contrôlé automatiquement.
  - [#MP: quel est le comportement par défaut du FriendBot? ]
  + **Démo**
     - il faut pouvoir distinguer par leur aspect, le joueur et son FriendBot
     - il faut pouvoir distinguer qui le joueur contrôle 



