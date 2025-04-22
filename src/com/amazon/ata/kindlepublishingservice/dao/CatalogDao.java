package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import javax.inject.Inject;
import java.util.List;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion bookKey = new CatalogItemVersion();
        bookKey.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> query = new DynamoDBQueryExpression<CatalogItemVersion>()
                .withHashKeyValues(bookKey)
                .withScanIndexForward(false) // latest version first
                .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, query);
        return results.isEmpty() ? null : results.get(0);
    }

    public CatalogItemVersion deleteBookFromCatalog(String bookId) {
        CatalogItemVersion book = getBookFromCatalog(bookId);
        book.setInactive(true);
        dynamoDbMapper.save(book);
        return book;
    }

    public void validateBookExists(String bookId) {
        if (getLatestVersionOfBook(bookId) == null) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
    }

    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook formattedBook) {
        if (formattedBook.getBookId() == null) {
            return createNewBook(formattedBook);
        } else {
            return updateExistingBook(formattedBook);
        }
    }

    private CatalogItemVersion createNewBook(KindleFormattedBook formattedBook) {
        CatalogItemVersion newBook = new CatalogItemVersion();

        newBook.setBookId(KindlePublishingUtils.generateBookId());
        newBook.setVersion(1);
        newBook.setInactive(false);
        populateBookFields(newBook, formattedBook);

        dynamoDbMapper.save(newBook);
        return newBook;
    }

    private CatalogItemVersion updateExistingBook(KindleFormattedBook formattedBook) {
        CatalogItemVersion currentVersion = getBookFromCatalog(formattedBook.getBookId());

        // Mark current version inactive
        currentVersion.setInactive(true);
        dynamoDbMapper.save(currentVersion);

        // Create new version
        CatalogItemVersion newVersion = new CatalogItemVersion();
        newVersion.setBookId(currentVersion.getBookId());
        newVersion.setVersion(currentVersion.getVersion() + 1);
        newVersion.setInactive(false);
        populateBookFields(newVersion, formattedBook);

        dynamoDbMapper.save(newVersion);
        return newVersion;
    }

    private void populateBookFields(CatalogItemVersion target, KindleFormattedBook source) {
        target.setTitle(source.getTitle());
        target.setText(source.getText());
        target.setAuthor(source.getAuthor());
        target.setGenre(source.getGenre());
    }
}
