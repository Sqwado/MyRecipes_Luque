# Maquette — MyRecipes

## Voir les maquettes

Ouvre la maquette visuelle interactive dans un navigateur :

**[docs/maquette/index.html](maquette/index.html)**

Elle présente, sous forme de téléphones réalistes :

- Menu hamburger (drawer) — Categories, Cuisines, Search, Favorites, Surprise
- Categories / Cuisines / recettes (cards image-forward)
- Détail recette (hero, chips, YouTube, nutri-score, ingrédients illustrés, share / favoris / cook)
- Search
- Favorites

Palette et photos d’exemple TheMealDB pour se projeter dans l’UI XML finale.

## Identité visuelle

![Icône MyRecipes](assets/my_recipes_icon.jpg)

| Rôle | Couleur | Hex indicatif |
|------|---------|---------------|
| Fond / marque | Terracotta | `#8B4A3B` |
| Contours / texte fort | Brun chocolat | `#3D2314` |
| Accent dégradé haut | Pêche | `#F5C9B0` |
| Accent dégradé bas | Corail | `#E8917A` |
| Texte sur fond sombre | Crème | `#F5EDE6` |
| Surface écrans | Clair | `#FFF8F5` |

Ces valeurs sont mappées dans `res/values/colors.xml` (+ `values-night`).

## Direction UI

- Cards **photo-first** (titre en overlay)
- Drawer avec icônes vectorielles cohérentes
- États loading / vide / erreur + bandeau offline
- Bottom bar d’actions sur le détail (share · favoris · cuisiner)

## Navigation

```mermaid
flowchart LR
  Drawer --> Categories
  Drawer --> Cuisines
  Drawer --> Search
  Drawer --> Favorites
  Drawer --> Surprise
  Categories --> RecipesList
  Cuisines --> RecipesList
  RecipesList --> RecipeDetail
  Search --> RecipeDetail
  Surprise --> RecipeDetail
  RecipeDetail -->|add| Favorites
  RecipeDetail --> CookMode
```
