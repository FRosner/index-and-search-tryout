package de.frosner.tryout.index;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class App {

    public static final String NAME_FIELD = "name";
    public static final String PRODUCTS_FIELD = "products";
    public static final String LAT_FIELD= "lat";
    public static final String LON_FIELD = "lon";
    public static final String KPI_FIELD = "kpi";

    public static void main(String[] args) throws Exception {
        Directory dir = new RAMDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter writer = new IndexWriter(dir, conf);
        writer.addDocument(createPerson("Frank", Lists.newArrayList("Cakes", "Pencils"), 51.2, 11.0, 0.6));
        writer.addDocument(createPerson("Milos", Lists.newArrayList("Pencils", "Cars"), 52.5, 12.0, 0.15));
        writer.addDocument(createPerson("Marco", Lists.newArrayList("Pencils"), 52.5, 12.5, 0.1));
        for (int i = 0; i < 10000; i++) {
            writer.addDocument(createPerson("Person" + i, Lists.newArrayList("Dummy"), 40 + (double) i / 1000, 10 + (double) i / 1000, (double) i / 10000));
        }
        writer.forceMerge(1);
        writer.close();

        final IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.search(new MatchAllDocsQuery(), 10); // warmup
        submitQuery(searcher, new PersonScoreQuery(new PersonQuery(Sets.newHashSet("Pencils"), 50d, 10d)));
        submitQuery(searcher, new PersonScoreQuery(new PersonQuery(Sets.newHashSet("Cakes"), 50d, 10d)));
        submitQuery(searcher, new PersonScoreQuery(new PersonQuery(Sets.newHashSet("Cakes", "Pencils", "Dummy"), 50d, 10d)));
        submitQuery(searcher, new PersonScoreQuery(new PersonQuery(Sets.newHashSet("Cakes", "Dummy"), 50d, 10d)));
        submitQuery(searcher, new PersonScoreQuery(new PersonQuery(Sets.newHashSet("Cars"), 50d, 10d)));
        reader.close();
    }

    private static void submitQuery(final IndexSearcher searcher, Query query) throws IOException {
        System.out.println(String.format("[%s] Submitting query %s", new Date(), query));
        Date before = new Date();
        TopDocs result = searcher.search(query, 10);
        Date after = new Date();
        Iterable<String> topPersons = Iterables.transform(Arrays.asList(result.scoreDocs), new Function<ScoreDoc, String>() {
            @Override
            public String apply(ScoreDoc scoreDoc) {
                try {
                    Document document = searcher.getIndexReader().document(scoreDoc.doc);
                    String personName = document.getField(App.NAME_FIELD).stringValue();
                    return personName + " (" + scoreDoc.score + ")";
                } catch (IOException e) {
                    return "Cannot retrieve document: " + e;
                }
            }
        });
        System.out.println(String.format("[%s] Retrieved results in %s ms: %s", new Date(), after.getTime() - before.getTime(), Iterables.toString(topPersons)));
    }

    private static Document createPerson(String name, Iterable<String> products, double lat, double lon, double kpi) {
        Document doc = new Document();
        Field nameField = new StringField(NAME_FIELD, name, Field.Store.YES);
        Field productsField = new TextField(PRODUCTS_FIELD, Joiner.on(" ").join(products), Field.Store.YES);
        Field latField = new DoubleField(LAT_FIELD, lat, Field.Store.YES);
        Field lonField = new DoubleField(LON_FIELD, lon, Field.Store.YES);
        Field kpiField = new DoubleField(KPI_FIELD, kpi, Field.Store.YES);
        doc.add(nameField);
        doc.add(productsField);
        doc.add(latField);
        doc.add(lonField);
        doc.add(kpiField);
        return doc;
    }

}
