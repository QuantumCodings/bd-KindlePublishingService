package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.clients.RecommendationsServiceClient;
import com.amazon.ata.kindlepublishingservice.converters.CatalogItemConverter;
import com.amazon.ata.kindlepublishingservice.converters.RecommendationsCoralConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.models.requests.GetBookRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetBookResponse;
import com.amazon.ata.recommendationsservice.types.BookGenre;
import com.amazon.ata.recommendationsservice.types.BookRecommendation;

import javax.inject.Inject;
import java.util.List;

public class GetBookActivity {

    private final CatalogDao catalogDao;
    private final RecommendationsServiceClient recommendationClient;

    @Inject
    public GetBookActivity(CatalogDao catalogDao, RecommendationsServiceClient recommendationClient) {
        this.catalogDao = catalogDao;
        this.recommendationClient = recommendationClient;
    }

    public GetBookResponse execute(final GetBookRequest request) {
        // Fetch the catalog item by ID
        CatalogItemVersion catalogItem = fetchCatalogItem(request);

        // Retrieve recommendations based on genre
        List<BookRecommendation> recommendations = fetchRecommendations(catalogItem);

        // Convert catalog item and recommendations to response model
        return createResponse(catalogItem, recommendations);
    }

    private CatalogItemVersion fetchCatalogItem(GetBookRequest request) {
        return catalogDao.getBookFromCatalog(request.getBookId());
    }

    private List<BookRecommendation> fetchRecommendations(CatalogItemVersion catalogItem) {
        BookGenre genre = convertGenre(catalogItem);
        return recommendationClient.getBookRecommendations(genre);
    }

    private BookGenre convertGenre(CatalogItemVersion catalogItem) {
        return BookGenre.valueOf(catalogItem.getGenre().name());
    }

    private GetBookResponse createResponse(CatalogItemVersion catalogItem, List<BookRecommendation> recommendations) {
        return GetBookResponse.builder()
                .withBook(CatalogItemConverter.toBook(catalogItem))
                .withRecommendations(RecommendationsCoralConverter.toCoral(recommendations))
                .build();
    }
}

