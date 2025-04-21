package com.amazon.ata.kindlepublishingservice.models.response;
import java.util.Objects;

public class RemoveBookFromCatalogResponse {
    private String bookId;
    private int version;
    private boolean inactive;

    // Private constructor
    public RemoveBookFromCatalogResponse(Builder builder) {
        this.bookId = builder.bookId;
        this.version = builder.version;
        this.inactive = builder.inactive;
    }
    
    public String getBookId() {
        return bookId;
    }
    public int getVersion() {
        return version;
    }
    public boolean isInactive() {
        return inactive;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoveBookFromCatalogResponse that = (RemoveBookFromCatalogResponse) o;
        return version == that.version &&
                inactive == that.inactive &&
                Objects.equals(bookId, that.bookId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(bookId, version, inactive);
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        private String bookId;
        private int version;
        private boolean inactive;
        private Builder() {
        }

        public Builder withBookId(String bookId) {
            this.bookId = bookId;
            return this;
        }

        public Builder withVersion(int version) {
            this.version = version;
            return this;
        }

        public Builder withInactive(boolean inactive) {
            this.inactive = inactive;
            return this;
        }

        public RemoveBookFromCatalogResponse build() {
            return new RemoveBookFromCatalogResponse(this);
        }
    }
}
