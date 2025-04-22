package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;
    private final BookPublishRequestManager bookPublishRequestManager;

    @Inject
    public BookPublishTask(PublishingStatusDao publishingStatusDao,
                           CatalogDao catalogDao,
                           BookPublishRequestManager bookPublishRequestManager) {
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
        this.bookPublishRequestManager = bookPublishRequestManager;
    }

    @Override
    public void run() {
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();

        if (request == null) {
            // No book requests to process
            return;
        }

        processPublishingRequest(request);
    }

    private void processPublishingRequest(BookPublishRequest request) {
        markInProgress(request);

        KindleFormattedBook formattedBook = formatBook(request);

        try {
            CatalogItemVersion itemVersion = catalogDao.createOrUpdateBook(formattedBook);
            markSuccessful(request, itemVersion.getBookId());
        } catch (BookNotFoundException e) {
            markFailed(request, e.getMessage());
        }
    }

    private void markInProgress(BookPublishRequest request) {
        publishingStatusDao.setPublishingStatus(
                request.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS,
                request.getBookId()
        );
    }

    private KindleFormattedBook formatBook(BookPublishRequest request) {
        return KindleFormatConverter.format(request);
    }

    private void markSuccessful(BookPublishRequest request, String bookId) {
        publishingStatusDao.setPublishingStatus(
                request.getPublishingRecordId(),
                PublishingRecordStatus.SUCCESSFUL,
                bookId
        );
    }

    private void markFailed(BookPublishRequest request, String errorMessage) {
        publishingStatusDao.setPublishingStatus(
                request.getPublishingRecordId(),
                PublishingRecordStatus.FAILED,
                request.getBookId(),
                errorMessage
        );
    }
}
