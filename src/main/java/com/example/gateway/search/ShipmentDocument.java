package com.example.gateway.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "shipments")
public record ShipmentDocument(
        @Id String trackingNumber,
        @Field(type = FieldType.Keyword) String carrier,
        @Field(type = FieldType.Keyword) String status,
        @Field(type = FieldType.Text) String origin,
        @Field(type = FieldType.Text) String destination
) {
}
