package de.frosner.tryout.index;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class PersonScoreQuery extends CustomScoreQuery {

    private Query query;

    public PersonScoreQuery(Query subQuery) {
        super(subQuery);
        this.query = subQuery;
    }

    public PersonScoreQuery(Query subQuery, FunctionQuery scoringQuery) {
        super(subQuery, scoringQuery);
        this.query = subQuery;
    }

    public PersonScoreQuery(Query subQuery, FunctionQuery... scoringQueries) {
        super(subQuery, scoringQueries);
        this.query = subQuery;
    }

    @Override
    public CustomScoreProvider getCustomScoreProvider(final LeafReaderContext reader) {
        return new CustomScoreProvider(reader) {
            @Override
            public float customScore(int doc,
                                     float subQueryScore,
                                     float valSrcScore) throws IOException {
                Document document = reader.reader().document(doc);
                Set<String> products = Sets.newHashSet(document.getField(App.PRODUCTS_FIELD).stringValue().split(" "));
                double lat = document.getField(App.LAT_FIELD).numericValue().doubleValue();
                double lon = document.getField(App.LON_FIELD).numericValue().doubleValue();
                double kpi = document.getField(App.KPI_FIELD).numericValue().doubleValue();
                PersonQuery personQuery = (PersonQuery) query;
                Set<String> personProducts = personQuery.getProductInterests();
                double productScore = ((double) Sets.intersection(personProducts, products).size()) / personProducts.size();
                double locationScore = 1d / Math.sqrt(Math.pow(personQuery.getLat() - lat, 2) + Math.pow(personQuery.getLon() - lon, 2));
                return (float) (locationScore * kpi * productScore);
            }
        };
    }

}
