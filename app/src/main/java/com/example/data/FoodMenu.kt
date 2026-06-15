package com.example.data

data class FoodItem(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val description: String,
    val rating: Float = 4.5f,
    val reviewCount: Int = 12,
    val imageUrl: String = "",
    val discountPercent: Int = 0
) {
    val discountedPrice: Double
        get() = if (discountPercent > 0) price * (1.0 - discountPercent / 100.0) else price
}

object FoodMenu {
    val categories = listOf(
        "All",
        "Combos",
        "Mo:Mo Special",
        "Burgers & Pizza",
        "Traditional Khaja",
        "Main Course",
        "Drinks & Desserts"
    )

    val menuList = listOf(
        FoodItem(
            id = "combo_family",
            name = "Family Combo",
            price = 1000.0,
            category = "Combos",
            description = "1 Chicken Burger, 1 Chicken Chowmine, 1 Buff Mo:Mo, and 500ml Coke. Chef's supreme recommendation!",
            rating = 4.9f,
            reviewCount = 210,
            discountPercent = 0
        ),
        FoodItem(
            id = "combo_mini",
            name = "Mini Combo",
            price = 800.0,
            category = "Combos",
            description = "Perfect single-person feast with a Mini Burger, small chow mein, and soft drink.",
            rating = 4.7f,
            reviewCount = 65,
            discountPercent = 5
        ),
        FoodItem(
            id = "combo_royal",
            name = "Royal Combo",
            price = 1000.0,
            category = "Combos",
            description = "Rich platter of special Biryani, chicken wings, lassi, and gulab jamun.",
            rating = 4.8f,
            reviewCount = 88,
            discountPercent = 5
        ),
        FoodItem(
            id = "burger_cheese",
            name = "Chicken Cheese Burger",
            price = 290.0,
            category = "Burgers & Pizza",
            description = "Freshly grilled juicy chicken patty, melted cheddar cheese, fresh lettuce, tomatoes, and custom burger sauce in a toasted bun.",
            rating = 4.6f,
            reviewCount = 145,
            discountPercent = 5
        ),
        FoodItem(
            id = "pizza_chicken_8",
            name = "Chicken Pizza Small (8 inch)",
            price = 480.0,
            category = "Burgers & Pizza",
            description = "Authentic stone-baked crust topped with chunks of seasoned chicken, rich marinara sauce, mozzarella, and green peppers.",
            rating = 4.5f,
            reviewCount = 54,
            discountPercent = 5
        ),
        FoodItem(
            id = "pizza_mexicana_12",
            name = "Mexicana Veg Pizza Large (12 inch)",
            price = 700.0,
            category = "Burgers & Pizza",
            description = "Generous large pizza loaded with spicy jalapeños, sweet corn, mushrooms, olives, onions, and spicy Mexican spices.",
            rating = 4.4f,
            reviewCount = 38,
            discountPercent = 5
        ),
        FoodItem(
            id = "momo_buff",
            name = "Buff Mo:Mo",
            price = 200.0,
            category = "Mo:Mo Special",
            description = "Traditional Nepali style steamed dumplings filled with spiced minced buffalo meat, served with hot spicy sesame chutney.",
            rating = 4.8f,
            reviewCount = 420,
            discountPercent = 0
        ),
        FoodItem(
            id = "momo_chicken_fry",
            name = "Chicken Fry Mo:Mo",
            price = 220.0,
            category = "Mo:Mo Special",
            description = "Crispy golden deep-fried chicken dumplings, crunchy on the outside, incredibly juicy on the inside.",
            rating = 4.7f,
            reviewCount = 195,
            discountPercent = 0
        ),
        FoodItem(
            id = "momo_veg",
            name = "Veg Mo:Mo",
            price = 160.0,
            category = "Mo:Mo Special",
            description = "Wholesome steamed dumplings stuffed with finely minced fresh garden vegetables and authentic Himalayan spices.",
            rating = 4.4f,
            reviewCount = 112,
            discountPercent = 5
        ),
        FoodItem(
            id = "khaja_mutton",
            name = "Mutton Khaja Set",
            price = 600.0,
            category = "Traditional Khaja",
            description = "A comprehensive traditional Newari platter featuring delicious mutton sekuwa/choila, chiura (beaten rice), bhatmas sandheko, aloo tama, pickles, and green salad.",
            rating = 4.9f,
            reviewCount = 156,
            discountPercent = 5
        ),
        FoodItem(
            id = "khaja_veg",
            name = "Veg Khaja Set",
            price = 200.0,
            category = "Traditional Khaja",
            description = "Authentic Nepali vegetarian afternoon platter with beaten rice, spiced potato fingers, peanut sandheko, and traditional pickles.",
            rating = 4.2f,
            reviewCount = 47,
            discountPercent = 5
        ),
        FoodItem(
            id = "pasta_mix",
            name = "Mix Pasta (Non Veg)",
            price = 450.0,
            category = "Main Course",
            description = "Stir-fried penne pasta tossed with tender chicken slices, cheese, garden fresh vegetables, creamy Alfredo and marinara blend sauces.",
            rating = 4.5f,
            reviewCount = 63,
            discountPercent = 5
        ),
        FoodItem(
            id = "chicken_chilli_boneless",
            name = "Chicken Chilli",
            price = 380.0,
            category = "Main Course",
            description = "Wok-tossed battered crispy chicken chunks with diced bell peppers, onions, green chillies, and savory ginger-garlic soy glaze.",
            rating = 4.6f,
            reviewCount = 189,
            discountPercent = 0
        ),
        FoodItem(
            id = "paneer_chilli",
            name = "Paneer Chilli",
            price = 350.0,
            category = "Main Course",
            description = "Cottage cheese (paneer) cubes crisp fried and wok-tossed with fresh peppers, onions, hot green chillies, and dry Indo-Chinese chili sauce.",
            rating = 4.5f,
            reviewCount = 92,
            discountPercent = 5
        ),
        FoodItem(
            id = "lassi_sweet",
            name = "Sweet Lassi",
            price = 120.0,
            category = "Drinks & Desserts",
            description = "Refreshing traditional sweet yogurt drink blended with cardamom, ice, and topped with dry fruits and pistachios.",
            rating = 4.7f,
            reviewCount = 204,
            discountPercent = 5
        ),
        FoodItem(
            id = "dessert_forest",
            name = "Black Forest Cake",
            price = 700.0,
            category = "Drinks & Desserts",
            description = "Decadent layered chocolate sponge cake filled with sweet cherries, whipped cream frosting, and topped with rich chocolate shavings.",
            rating = 4.8f,
            reviewCount = 76,
            discountPercent = 0
        ),
        FoodItem(
            id = "dessert_haluwa",
            name = "Gajar Haluwa",
            price = 200.0,
            category = "Drinks & Desserts",
            description = "Warm delicious carrot pudding slow-cooked in sweetened whole milk, clarified butter (ghee), flavored with saffron, cardamom, cashew-raisins.",
            rating = 4.6f,
            reviewCount = 49,
            discountPercent = 0
        )
    )
}
