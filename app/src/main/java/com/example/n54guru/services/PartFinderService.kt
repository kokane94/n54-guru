package com.example.n54guru.services

import org.jsoup.Jsoup
import com.example.n54guru.models.PartSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PartFinderService {

    suspend fun searchAliExpress(query: String): List<PartSearchResult> = withContext(Dispatchers.IO) {
        searchWebsite("AliExpress", "https://www.aliexpress.com/wholesale?catId=0&initiative_id=SB_20230101000000&SearchText=", query)
    }

    suspend fun searchAmazon(query: String): List<PartSearchResult> = withContext(Dispatchers.IO) {
        searchWebsite("Amazon", "https://www.amazon.com/s?k=", query)
    }

    suspend fun searcheBay(query: String): List<PartSearchResult> = withContext(Dispatchers.IO) {
        searchWebsite("eBay", "https://www.ebay.com/sch/i.html?_nkw=", query)
    }

    private suspend fun searchWebsite(sourceName: String, baseUrl: String, query: String): List<PartSearchResult> {
        val results = mutableListOf<PartSearchResult>()
        val searchUrl = baseUrl + query.replace(" ", "+")

        try {
            // IMPORTANT: Web scraping is highly dependent on the website's HTML structure.
            // The selectors used here are placeholders and will likely need to be updated
            // frequently as websites change. Consider using official APIs if available.
            // Also, be mindful of terms of service and rate limits when scraping.

            val document = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000) // 10 seconds timeout
                .get()

            // Placeholder for actual scraping logic. This needs to be implemented
            // with specific CSS selectors for each website.
            // Example for a generic product listing:
            // val productElements = document.select("div.product-item")
            // productElements.forEach { productElement ->
            //    val name = productElement.select("h2.product-title a").text()
            //    val price = productElement.select("span.product-price").text()
            //    val link = productElement.select("h2.product-title a").attr("href")
            //    if (name.isNotEmpty() && price.isNotEmpty() && link.isNotEmpty()) {
            //        results.add(PartSearchResult(name, price, link, sourceName))
            //    }
            // }

            // For now, return a dummy result to show the UI functionality
            results.add(
                PartSearchResult(
                    name = "[PLACEHOLDER] $sourceName Part for '$query'",
                    price = "$XX.XX",
                    link = searchUrl,
                    source = sourceName
                )
            )

        } catch (e: Exception) {
            println("Error scraping $sourceName for '$query': ${e.message}")
            // Optionally add an error result
            results.add(
                PartSearchResult(
                    name = "Error searching $sourceName for '$query'",
                    price = "N/A",
                    link = "#",
                    source = sourceName
                )
            )
        }
        return results
    }
}
