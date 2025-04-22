package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;

import javax.inject.Inject;

public class SubmitBookForPublishingActivity {

    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;
    private final BookPublishRequestManager publishRequestManager;

    @Inject
    public SubmitBookForPublishingActivity(PublishingStatusDao publishingStatusDao,
                                           CatalogDao catalogDao,
                                           BookPublishRequestManager publishRequestManager) {
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
        this.publishRequestManager = publishRequestManager;
    }

    public SubmitBookForPublishingResponse execute(SubmitBookForPublishingRequest request) {
        BookPublishRequest publishRequest = convertToPublishRequest(request);

        if (isUpdateRequest(publishRequest)) {
            validateBookExists(publishRequest.getBookId());
        }

        enqueuePublishingRequest(publishRequest);

        PublishingStatusItem statusItem = markPublishingAsQueued(publishRequest);

        return buildResponse(statusItem);
    }

    private BookPublishRequest convertToPublishRequest(SubmitBookForPublishingRequest request) {
        return BookPublishRequestConverter.toBookPublishRequest(request);
    }

    private boolean isUpdateRequest(BookPublishRequest request) {
        return request.getBookId() != null;
    }

    private void validateBookExists(String bookId) {
        catalogDao.validateBookExists(bookId);
    }

    private void enqueuePublishingRequest(BookPublishRequest request) {
        publishRequestManager.addBookPublishRequest(request);
    }

    private PublishingStatusItem markPublishingAsQueued(BookPublishRequest request) {
        return publishingStatusDao.setPublishingStatus(
                request.getPublishingRecordId(),
                PublishingRecordStatus.QUEUED,
                request.getBookId()
        );
    }

    private SubmitBookForPublishingResponse buildResponse(PublishingStatusItem item) {
        return SubmitBookForPublishingResponse.builder()
                .withPublishingRecordId(item.getPublishingRecordId())
                .build();
    }
}
