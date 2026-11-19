# Documentation API — TheMealDB

Source officielle : [https://www.themealdb.com/api.php](https://www.themealdb.com/api.php)

## Généralités

| Élément | Valeur |
|--------|--------|
| Base URL (V1) | `https://www.themealdb.com/api/json/v1/1/` |
| Clé de test | `1` (usage éducatif / développement) |
| Format | JSON |
| Auth | Clé intégrée dans l’URL (`/v1/{key}/`) |

Pour une publication Play Store publique, TheMealDB exige une clé supporter. En phase scolaire, la clé `1` suffit pour le développement et les revues.

## Endpoints utilisés par MyRecipes

### 1. Liste des catégories — besoin B2

```
GET https://www.themealdb.com/api/json/v1/1/categories.php
```

| Champ | Usage app |
|-------|-----------|
| `idCategory` | Identifiant interne |
| `strCategory` | Nom affiché + paramètre pour le filtre recettes |
| `strCategoryThumb` | Vignette de la ligne |

### 2. Recettes par catégorie — besoin B4

```
GET https://www.themealdb.com/api/json/v1/1/filter.php?c={CategoryName}
```

| Champ | Usage app |
|-------|-----------|
| `idMeal` | Navigation vers le détail |
| `strMeal` | Nom de la recette |
| `strMealThumb` | Vignette |

### 3. Détail d’une recette — besoin B6

```
GET https://www.themealdb.com/api/json/v1/1/lookup.php?i={idMeal}
```

| Champ | Usage app |
|-------|-----------|
| `idMeal` | Identifiant |
| `strMeal` | Titre de l’écran |
| `strCategory` / `strArea` | Chips meta |
| `strMealThumb` | Photo hero |
| `strInstructions` | Étapes |
| `strIngredientN` / `strMeasureN` | Ingrédients + quantités |
| `strYoutube` | Bouton vidéo (si non vide) |

### 4. Recherche par nom — besoin B11

```
GET https://www.themealdb.com/api/json/v1/1/search.php?s={name}
```

### 5. Recette aléatoire — Surprise (hors CDC)

```
GET https://www.themealdb.com/api/json/v1/1/random.php
```

Réponse : même forme que `lookup.php` (un meal). Ouverture directe du détail.

### 6. Liste des cuisines / zones — hors CDC

```
GET https://www.themealdb.com/api/json/v1/1/list.php?a=list
```

Puis recettes :

```
GET https://www.themealdb.com/api/json/v1/1/filter.php?a={AreaName}
```

## Images

### Catégories / recettes

URLs fournies par l’API (`strCategoryThumb`, `strMealThumb`). Les cards image-forward et le hero chargent la **photo pleine** (pas `/preview`, trop pixelisée à cette taille).

### Ingrédients

```
https://www.themealdb.com/images/ingredients/{Name}.png
https://www.themealdb.com/images/ingredients/{Name}-Small.png
```

Espaces remplacés par `_`. Utilisé sur le détail et le mode cuisine.

## Nutri-score (besoin B7) — hors API

TheMealDB **ne fournit pas** de calories ni de nutri-score.

Choix de conception : calcul local heuristique (`NutriScoreCalculator`) + Custom View `NutriScoreView` (rouge → vert).

## Mapping besoins ↔ API

| Besoin | Écran | Endpoint |
|--------|-------|----------|
| B2–B3 | Categories | `categories.php` |
| B4–B5 | `{Category} Recipes` | `filter.php?c=` |
| B6 | Détail recette | `lookup.php?i=` |
| B7 | Nutri-score | Calcul local |
| B8–B10 | Favorites | Room |
| B11 | Search | `search.php?s=` |
| B12 | Menu hamburger | Navigation locale |
| + | Surprise | `random.php` |
| + | Cuisines | `list.php?a=list` + `filter.php?a=` |
| + | YouTube | champ `strYoutube` |
| + | Images ingrédients | CDN ingredients |

## Attribution

Données et images fournies par [TheMealDB](https://www.themealdb.com/).
