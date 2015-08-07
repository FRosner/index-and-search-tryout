package de.frosner.tryout.index;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.ToStringUtils;

import java.util.Objects;
import java.util.Set;

public class PersonQuery extends BooleanQuery {

    private final Set<String> productInterests;
    private final double lat;
    private final double lon;

    public Set<String> getProductInterests() {
        return productInterests;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public PersonQuery(Set<String> productInterests, double lat, double lon) {
        BooleanQuery innerQuery = new BooleanQuery();
        for (String productInterest : productInterests) {
            innerQuery.add(new BooleanClause(
                            new TermQuery(new Term(App.PRODUCTS_FIELD, productInterest)), BooleanClause.Occur.SHOULD)
            );
        }
        super.add(new BooleanClause(innerQuery, BooleanClause.Occur.MUST));
        this.productInterests = productInterests;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersonQuery == false) {
            return false;
        }
        final PersonQuery that = (PersonQuery) obj;
        return super.equals(obj) && productInterests.equals(that.productInterests) &&
            lat == that.lat && lon == that.lon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), productInterests, lat, lon, getBoost());
    }

    @Override
    public String toString(String field) {
        return "PersonQuery [productInterests=" + this.productInterests + ", " +
            "lat=" + this.lat + ", " + "lon=" + this.lon+ "] " +
            ToStringUtils.boost(getBoost());
    }

}
