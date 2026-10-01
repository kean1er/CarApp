package com.carscan.app

object Models {
    val all = listOf(
        "Lada Granta", "Lada Vesta", "Lada Niva Legend", "Lada Niva Travel", "Lada Largus", "Lada Priora", "Lada Kalina", "Lada 2107",
        "UAZ Patriot", "UAZ Hunter", "GAZ Gazelle",
        "Toyota Camry", "Toyota Corolla", "Toyota RAV4", "Toyota Land Cruiser", "Toyota Land Cruiser Prado", "Toyota Supra", "Toyota Highlander",
        "Honda Civic", "Honda Accord", "Honda CR-V",
        "Nissan Skyline GT-R", "Nissan Qashqai", "Nissan X-Trail", "Nissan Juke", "Nissan Almera",
        "Mazda 3", "Mazda 6", "Mazda CX-5", "Mazda RX-7", "Mazda MX-5",
        "Mitsubishi Lancer Evolution", "Mitsubishi Outlander", "Mitsubishi Pajero",
        "Subaru Impreza WRX STI", "Subaru Forester", "Subaru Outback",
        "BMW 3 Series", "BMW 5 Series", "BMW 7 Series", "BMW X5", "BMW X6", "BMW M3", "BMW M5",
        "Mercedes-Benz C-Class", "Mercedes-Benz E-Class", "Mercedes-Benz S-Class", "Mercedes-Benz G-Class", "Mercedes-Benz GLE",
        "Audi A4", "Audi A6", "Audi Q7", "Audi RS6", "Audi TT",
        "Volkswagen Golf", "Volkswagen Passat", "Volkswagen Polo", "Volkswagen Tiguan", "Volkswagen Touareg",
        "Skoda Octavia", "Skoda Rapid", "Skoda Superb",
        "Kia Rio", "Kia Ceed", "Kia Sportage", "Kia K5", "Kia Optima",
        "Hyundai Solaris", "Hyundai Creta", "Hyundai Tucson", "Hyundai Sonata",
        "Ford Focus", "Ford Mustang", "Ford Mondeo",
        "Chevrolet Niva", "Chevrolet Camaro", "Chevrolet Cruze",
        "Renault Logan", "Renault Duster", "Renault Sandero",
        "Lexus RX", "Lexus LX", "Lexus IS",
        "Porsche 911", "Porsche Cayenne", "Porsche Panamera",
        "Tesla Model 3", "Tesla Model S", "Tesla Model Y",
        "Geely Coolray", "Chery Tiggo 7", "Haval Jolion", "Haval F7"
    )

    fun suggest(q: String): List<String> {
        val s = q.trim().lowercase()
        if (s.length < 2) return emptyList()
        return all.filter { it.lowercase().contains(s) }.take(8)
    }
}
