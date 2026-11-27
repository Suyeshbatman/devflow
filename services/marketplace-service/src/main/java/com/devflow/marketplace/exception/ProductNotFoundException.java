package com.devflow.marketplace.exception;

import java.util.UUID;

// [CLASS] Thrown when a product doesn't exist OR is archived. Both look the same to the client → 404.
public class ProductNotFoundException extends RuntimeException {
    // [CONSTRUCTOR] [PARAMETER] id: the product that wasn't found.
    public ProductNotFoundException(UUID id){
        // [CONSTRUCTOR CALL] super(...) calls the parent RuntimeException constructor to set the message.
        super("Product with id " + id + " was not found");
    }
}
