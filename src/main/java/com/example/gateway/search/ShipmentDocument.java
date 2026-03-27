package com.example.gateway.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentDocument {

    @Id
    private String trackingNumber;

    @Field(type = FieldType.Keyword)
    private String carrier;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String origin;

    @Field(type = FieldType.Text)
    private String destination;
}
