package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

public class CatalogDao {

    private static final Logger LOGGER = LogManager.getLogger(BookPublishRequestManager.class);

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + bookId + " not found in catalog.");
        }
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
    public void save(CatalogItemVersion catalogItemVersion) {
        dynamoDbMapper.save(catalogItemVersion);
    }

    public String createOrUpdateBook(BookPublishRequest request) {
        String bookId = StringUtils.isBlank(request.getBookId()) ?
                "book." + UUID.randomUUID().toString() :
                request.getBookId();

        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        int newVersion = (book == null) ? 1 : book.getVersion() + 1;
        if (book != null) {
            book.setInactive(true);
            save(book);
        }
        CatalogItemVersion newBookVersion = new CatalogItemVersion();
        newBookVersion.setBookId(bookId);
        newBookVersion.setTitle(request.getTitle());
        newBookVersion.setAuthor(request.getAuthor());
        newBookVersion.setGenre(request.getGenre());
        newBookVersion.setText(request.getText());
        newBookVersion.setVersion(newVersion);
        newBookVersion.setInactive(false);
        save(newBookVersion);
        return newBookVersion.getBookId();
    }


}
