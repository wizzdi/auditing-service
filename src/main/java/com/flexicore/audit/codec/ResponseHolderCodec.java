package com.flexicore.audit.codec;

import com.flexicore.audit.model.ResponseHolder;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

import java.util.HashMap;
import java.util.Map;

public class ResponseHolderCodec implements Codec<ResponseHolder> {

    private final Codec<Document> documentCodec;

    public ResponseHolderCodec() {
        this.documentCodec=new DocumentCodec();
    }

    @Override
    public ResponseHolder decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);

        Map<String,Object> map=new HashMap<>(document);
        return new ResponseHolder(map);
    }

    @Override
    public void encode(BsonWriter writer, ResponseHolder value, EncoderContext encoderContext) {
        Document document = new Document(value.get());
        documentCodec.encode(writer, document, encoderContext);


    }

    @Override
    public Class<ResponseHolder> getEncoderClass() {
        return ResponseHolder.class;
    }
}
