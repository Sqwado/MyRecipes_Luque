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

Réponse (extrait) :

```json
{
  "categories": [
    {
      "idCategory": "1",
      "strCategory": "Beef",
      "strCategoryThumb": "https://www.themealdb.com/images/category/beef.png",
      "strCategoryDescription": "..."
    }
  ]
}
```

| Champ | Usage app |
|-------|-----------|
| `idCategory` | Identifiant interne |
| `strCategory` | Nom affiché + paramètre pour le filtre recettes |
| `strCategoryThumb` | Vignette de la ligne |
| `strCategoryDescription` | Non requis par le cahier des charges |

### 2. Recettes par catégorie — besoin B4

```
GET https://www.themealdb.com/api/json/v1/1/filter.php?c={CategoryName}
```

Exemple : `filter.php?c=Seafood`

Réponse (extrait) :

```json
{
  "meals": [
    {
      "strMeal": "Baked salmon with fennel & tomatoes",
      "strMealThumb": "https://www.themealdb.com/images/media/meals/1548772327.jpg",
      "idMeal": "52959"
    }
  ]
}
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

Exemple : `lookup.php?i=52772`

Champs utiles :

| Champ | Usage app |
|-------|-----------|
| `idMeal` | Identifiant |
| `strMeal` | Titre de l’écran |
| `strCategory` | Catégorie |
| `strArea` | Pays / zone d’origine |
| `strMealThumb` | Photo du résultat |
| `strInstructions` | Instructions détaillées |
| `strIngredient1` … `strIngredient20` | Ingrédients (ignorer vides / null) |
| `strMeasure1` … `strMeasure20` | Quantités associées |
| `strYoutube` | Non requis par le cahier des charges |
| `strTags` | Non requis |

### 4. Recherche par nom — besoin B11

```
GET https://www.themealdb.com/api/json/v1/1/search.php?s={name}
```

Exemple : `search.php?s=Arrabiata`

- Si des résultats existent : `meals` est un tableau d’objets détail (même forme que `lookup`).
- Si aucun résultat : `"meals": null`.

## Images

### Catégories

URL fournie directement dans `strCategoryThumb`.

### Recettes

- URL pleine : `strMealThumb`
- Variantes possibles (doc TheMealDB) : suffixe `/preview`, ou tailles `/small`, `/medium`, `/large` selon les URLs d’images média.

### Ingrédients (optionnel, hors CDC)

`https://www.themealdb.com/images/ingredients/{Name}.png`  
(espaces remplacés par `_`)

## Nutri-score (besoin B7) — hors API

TheMealDB **ne fournit pas** de calories ni de nutri-score.

Choix de conception MyRecipes : **calcul local heuristique** à partir des données disponibles, par exemple :

1. Compter les ingrédients non vides (`n`).
2. Normaliser un score `0…100` (ex. plus d’ingrédients / plats plus « riches » → score plus bas, ou mapping fixe par catégorie).
3. Afficher le résultat via une **Custom View** (barre ou cadran) : rouge (0 %) → vert (100 %).

La formule exacte sera documentée / implémentée dans la feature `nutri-score`.

## Endpoints non utilisés (référence)

| Endpoint | Raison |
|----------|--------|
| `search.php?f=` | Liste par lettre — non demandée |
| `random.php` | Non demandée |
| `list.php?c/a/i=list` | Listes brutes — `categories.php` suffit |
| `filter.php?i=` / `filter.php?a=` | Filtre ingrédient / zone — non demandés |
| API V2 premium | Hors périmètre (clé `1`) |

## Mapping besoins ↔ API

| Besoin | Écran | Endpoint |
|--------|-------|----------|
| B2–B3 | Categories | `categories.php` |
| B4–B5 | `{Category} Recipes` | `filter.php?c=` |
| B6 | Détail recette | `lookup.php?i=` |
| B7 | Nutri-score | Calcul local |
| B8–B10 | Favorites | Persistance Room (pas d’API) |
| B11 | Search | `search.php?s=` |
| B12 | Menu hamburger | Navigation locale |

## Attribution

Données et images fournies par [TheMealDB](https://www.themealdb.com/).  
API et site restent gratuits au point d’accès ; respecter leurs conditions pour une éventuelle publication store.
