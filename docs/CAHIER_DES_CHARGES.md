# Cahier des charges — MyRecipes

Référence : projet de groupe Android Natif (Laurent Pastorelli), livraison au plus tard le **8 décembre 2026**.

## Besoins fonctionnels

| ID | Exigence |
|----|----------|
| **B1** | L’application s’appelle **MyRecipes**. |
| **B2** | Écran d’accueil **Categories** : liste des catégories TheMealDB ; chaque ligne = nom + vignette. |
| **B3** | Appui court sur une catégorie → ouvre l’écran des recettes (B4). |
| **B4** | Écran **`{CategoryName} Recipes`** : liste des recettes de la catégorie ; chaque ligne = nom + vignette. |
| **B5** | Appui court sur une recette → ouvre le détail (B6). |
| **B6** | Écran de détail (titre = nom de la recette) : catégorie, pays d’origine, photo, instructions, nutri-score (B7), ingrédients + quantités, bouton ajouter aux favoris. |
| **B7** | Indicateur nutritionnel (barre de couleur ou cadran) : **0 % (rouge) → 100 % (vert)** selon la valeur calorique du plat. |
| **B8** | Écran **Favorites** : liste des recettes favorites ; chaque ligne = nom + vignette + date/heure d’enregistrement. |
| **B9** | Moyen d’effacer une recette de la liste des favoris. |
| **B10** | Persistance des favoris en base locale. |
| **B11** | Écran **Search** : recherche par nom de recette → ouverture du détail (B6). |
| **B12** | Menu hamburger pour naviguer entre **Categories**, **Search**, **Favorites**. |

## Besoins non fonctionnels

| ID | Exigence |
|----|----------|
| **BN1** | Kotlin, `minSdk = 26`. |
| **BN2** | UI au choix : layouts XML **ou** Jetpack Compose. → **Choix projet : XML (Views)**. |
| **BN3** | Nutri-score implémenté avec une **Custom View** Android. |
| **BN4** | Favoris persistés avec **Jetpack Room**. |

## Stack prévue

- Kotlin + Android Views (XML)
- Navigation : Navigation Drawer (menu hamburger)
- Réseau : Retrofit / OkHttp (+ Glide ou Coil pour les images)
- Persistance : Room
- API : [TheMealDB](https://www.themealdb.com/api.php) — voir [API.md](API.md)

## Objectif qualité

Application de qualité suffisante pour un envoi Play Store : icône launcher, thème cohérent, gestion des états (chargement / vide / erreur), permission `INTERNET`, build release stable.
