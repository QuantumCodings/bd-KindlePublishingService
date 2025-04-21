package com.amazon.ata.kindlepublishingservice.activity;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.RemoveBookFromCatalogRequest;
import com.amazon.ata.kindlepublishingservice.models.response.RemoveBookFromCatalogResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;

public class RemoveBookFromCatalogActivity {

    private final CatalogDao catalogDao;

    @Inject
    public RemoveBookFromCatalogActivity(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    public RemoveBookFromCatalogResponse execute(RemoveBookFromCatalogRequest removeBookFromCatalogRequest) {
        String bookId = removeBookFromCatalogRequest.getBookId();
        CatalogItemVersion latestVersion = catalogDao.getBookFromCatalog(bookId);
        if (latestVersion == null || latestVersion.isInactive()) {
            throw new BookNotFoundException(String.format("Book with ID %s not found or already inactive.", bookId));
        }
        latestVersion.setInactive(true);
        catalogDao.save(latestVersion);

        return RemoveBookFromCatalogResponse.builder()
                .withBookId(latestVersion.getBookId())
                .withVersion(latestVersion.getVersion())
                .withInactive(true)
                .build();
    }
}
