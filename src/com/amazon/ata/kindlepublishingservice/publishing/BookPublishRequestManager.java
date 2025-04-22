package com.amazon.ata.kindlepublishingservice.publishing;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class BookPublishRequestManager {

    private final Queue<BookPublishRequest> requestQueue;

    @Inject
    public BookPublishRequestManager() {
        this.requestQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds a new BookPublishRequest to the processing queue.
     *
     * @param bookPublishRequest The book publish request to add.
     */
    public void addBookPublishRequest(BookPublishRequest bookPublishRequest) {
        if (bookPublishRequest == null) {
            throw new IllegalArgumentException("BookPublishRequest cannot be null.");
        }

        requestQueue.add(bookPublishRequest);
        // Optional: log.debug("Added BookPublishRequest to queue: {}", bookPublishRequest);
    }

    /**
     * Retrieves and removes the next BookPublishRequest to be processed from the queue.
     *
     * @return The next BookPublishRequest, or null if the queue is empty.
     */
    public BookPublishRequest getBookPublishRequestToProcess() {
        BookPublishRequest nextRequest = requestQueue.poll();
        // Optional: log.debug("Retrieved BookPublishRequest from queue: {}", nextRequest);
        return nextRequest;
    }

    /**
     * Checks whether there are any pending requests in the queue.
     *
     * @return true if the queue is not empty, false otherwise.
     */
    public boolean hasPendingRequests() {
        return !requestQueue.isEmpty();
    }
}
