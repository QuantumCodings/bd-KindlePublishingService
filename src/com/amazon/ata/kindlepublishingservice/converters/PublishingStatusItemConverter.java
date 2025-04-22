package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.List;
import java.util.ArrayList;

public class PublishingStatusItemConverter {
    private PublishingStatusItemConverter() {}

    /**
     * Converts a list of {@link PublishingStatusItem} to a list of {@link PublishingStatusRecord}.
     *
     * @param publishingStatusItems List of PublishingStatusItem to convert.
     * @return List of PublishingStatusRecord objects.
     */
    public static List<PublishingStatusRecord> toPublishingStatusRecord(List<PublishingStatusItem> publishingStatusItems) {
        List<PublishingStatusRecord> recordList = new ArrayList<>();

        for (PublishingStatusItem statusItem : publishingStatusItems) {
            recordList.add(convertItem(statusItem));
        }

        return recordList;
    }

    private static PublishingStatusRecord convertItem(PublishingStatusItem item) {
        PublishingStatusRecord.Builder builder = PublishingStatusRecord.builder()
                .withStatus(item.getStatus().name())
                .withStatusMessage(item.getStatusMessage());

        if (item.getBookId() != null) {
            builder.withBookId(item.getBookId());
        }

        return builder.build();
    }
}
