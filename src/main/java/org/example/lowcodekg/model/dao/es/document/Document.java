package org.example.lowcodekg.model.dao.es.document;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.neo4j.core.schema.Id;

@Data
@org.springframework.data.elasticsearch.annotations.Document(indexName = "documents")
public class Document {

    @Id
    private String id;

    private Long neo4jId;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Dense_Vector, dims = 384)
    private float[] embedding;
}
