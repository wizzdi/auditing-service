package com.flexicore.audit.data;

import com.flexicore.annotations.plugins.PluginInfo;
import com.flexicore.audit.codec.ParameterHolderCodec;
import com.flexicore.audit.codec.ResponseHolderCodec;
import com.flexicore.audit.model.*;
import com.flexicore.audit.request.AuditingFilter;
import com.flexicore.interfaces.AbstractNoSqlRepositoryPlugin;
import com.flexicore.request.GetClassInfo;
import com.flexicore.utils.InheritanceUtils;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.flexicore.service.MongoConnectionService.MONGO_DB;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static java.util.regex.Pattern.compile;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Created by Asaf on 01/12/2016.
 */
@Component
@Extension
@PluginInfo(version = 1)
public class AuditingRepository extends AbstractNoSqlRepositoryPlugin {
    public static final String AUDITING_COLLECION_NAME = "Auditing";

    private static final String DATE_OCCURRED = "dateOccurred";
    private static final String AUDITING_TYPE = "auditingType";
    private static final String OPERATION_ID = "operationHolder.operationId";
    private static final String USER_ID = "userHolder.userId";
    private static final String USER_NAME = "userHolder.name";
    private static final String OPERATION_NAME = "operationHolder.name";




    private Logger logger = Logger.getLogger(getClass().getCanonicalName());
    private static CodecRegistry pojoCodecRegistry;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    @Qualifier(MONGO_DB)
    private String mongoDBName;

    private CodecRegistry getRegistry() {
        if(pojoCodecRegistry==null){
            PojoCodecProvider.Builder builder = PojoCodecProvider.builder();
            builder.register(AuditingEvent.class, RequestHolder.class, ResponseHolder.class, ParameterHolder.class, OperationHolder.class,UserHolder.class);

            pojoCodecRegistry = fromRegistries(
                    CodecRegistries.fromCodecs(new ParameterHolderCodec(),new ResponseHolderCodec()),
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(builder.build()));
        }
        return pojoCodecRegistry;



    }

    @Override
    public void merge(Object o) {
        if (o instanceof AuditingEvent) {
            MongoDatabase db =mongoClient.getDatabase(mongoDBName);
            MongoCollection<AuditingEvent> collection = db.getCollection(AUDITING_COLLECION_NAME, AuditingEvent.class).withCodecRegistry(getRegistry());
            collection.insertOne((AuditingEvent) o);
        }

    }



    static Bson getAuditingPredicates(AuditingFilter eventFiltering) {
        Bson pred = null;
        if (eventFiltering.getFromDate() != null) {

            Date start = Date.from(eventFiltering.getFromDate().toInstant());
            Bson gte = gte(DATE_OCCURRED, start);
            pred = pred == null ? gte : and(pred, gte);
        }

        if (eventFiltering.getToDate() != null) {
            Date end = Date.from(eventFiltering.getToDate().toInstant());
            Bson lte = lte(DATE_OCCURRED, end);
            pred = pred == null ? lte : and(pred, lte);
        }



        if (eventFiltering.getOperations()!=null && !eventFiltering.getOperations().isEmpty()) {
            Set<String> operationsIds=eventFiltering.getOperations().stream().map(f->f.getId()).collect(Collectors.toSet());
            Bson eq = in(OPERATION_ID, operationsIds);
            pred = pred == null ? eq : and(pred, eq);
        }
        if (eventFiltering.getUsers()!=null && !eventFiltering.getUsers().isEmpty()) {
            Set<String> userIds=eventFiltering.getUsers().stream().map(f->f.getId()).collect(Collectors.toSet());
            Bson eq = in(USER_ID, userIds);
            pred = pred == null ? eq : and(pred, eq);
        }

        if (eventFiltering.getAuditTypeLike() != null) {
            Bson eq = regex(AUDITING_TYPE, compile(eventFiltering.getAuditTypeLike()));
            pred = pred == null ? eq : and(pred, eq);
        }

        if (eventFiltering.getUserNameLike() != null) {

            Bson eq = Filters.regex(USER_NAME, compile(eventFiltering.getUserNameLike()));
            pred = pred == null ? eq : and(pred, eq);
        }

        if (eventFiltering.getOperationNameLike() != null) {

            Bson eq = Filters.regex(OPERATION_NAME, compile(eventFiltering.getOperationNameLike()));
            pred = pred == null ? eq : and(pred, eq);
        }


        return pred;
    }

    public  List<AuditingEvent> listAllAuditingEvents(AuditingFilter auditingFilter) {
        MongoDatabase db = mongoClient.getDatabase(mongoDBName).withCodecRegistry(getRegistry());
        MongoCollection<AuditingEvent> collection = db.getCollection(AUDITING_COLLECION_NAME, AuditingEvent.class).withCodecRegistry(getRegistry());

        Bson pred = getAuditingPredicates(auditingFilter);

        FindIterable<AuditingEvent> base = pred == null ? collection.find(AuditingEvent.class) : collection.find(pred, AuditingEvent.class);
        FindIterable<AuditingEvent> iter = base.sort(orderBy(descending(DATE_OCCURRED)));
        if (auditingFilter.getCurrentPage() != null && auditingFilter.getPageSize() != null && auditingFilter.getCurrentPage() > -1 && auditingFilter.getPageSize() > 0) {
            iter.limit(auditingFilter.getPageSize()).skip(auditingFilter.getPageSize() * auditingFilter.getCurrentPage());
        }
        List<AuditingEvent> alerts = new ArrayList<>();
        for (AuditingEvent alert : iter) {
            alerts.add(alert);
        }
        return alerts;
    }

    public long countAllAuditingEvents(AuditingFilter auditingFilter) {
        MongoDatabase db =mongoClient.getDatabase(mongoDBName).withCodecRegistry(pojoCodecRegistry);
        MongoCollection<AuditingEvent> collection = db.getCollection(AUDITING_COLLECION_NAME, AuditingEvent.class).withCodecRegistry(pojoCodecRegistry);

        Bson pred = getAuditingPredicates(auditingFilter);

        return pred == null ? collection.countDocuments() : collection.countDocuments(pred);

    }
}
