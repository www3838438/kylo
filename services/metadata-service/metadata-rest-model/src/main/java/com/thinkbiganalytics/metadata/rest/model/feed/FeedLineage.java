package com.thinkbiganalytics.metadata.rest.model.feed;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sr186054 on 11/11/16.
 */
public class FeedLineage {

    private Map<String, Feed> feedMap;

    private Feed feed;

    private Map<String, Datasource> datasourceMap = new HashMap<>();

    private Map<String,FeedLineageStyle> styles;

    public FeedLineage(Feed feed, Map<String,FeedLineageStyle> styles) {
        this.feed = feed;
        this.feedMap = new HashMap<>();
        this.styles = styles;
        serialize(this.feed);
    }

    @JsonIgnore
    private void serialize(Feed feed) {

        if (feed.getDependentFeeds() != null) {
            Set<String> ids = new HashSet<>();
            Set<Feed> dependentFeeds = new HashSet<>(feed.getDependentFeeds());
            feed.setDependentFeeds(null);
            dependentFeeds.stream().forEach(depFeed -> {
                                                feedMap.put(depFeed.getId(), depFeed);
                                                ids.add(depFeed.getId());

                                                if (depFeed.getDependentFeeds() != null) {
                                                    serialize(depFeed);
                                                }
                                            }
            );

            feed.setDependentFeedIds(ids);
        }
        if (feed.getUsedByFeeds() != null) {
            Set<String> ids = new HashSet<>();
            Set<Feed> usedByFeeds = new HashSet<>(feed.getUsedByFeeds());
            feed.getUsedByFeeds().clear();
            usedByFeeds.stream().forEach(depFeed -> {
                                             feedMap.put(depFeed.getId(), depFeed);
                                             ids.add(depFeed.getId());
                                             if (depFeed.getUsedByFeeds() != null) {
                                                 serialize(depFeed);
                                             }
                                         }
            );
            feed.setUsedByFeedIds(ids);
        }

        if (feed.getSources() != null) {
            feed.getSources().forEach(feedSource -> {
                Datasource ds = serializeDatasource(feedSource.getDatasource());
                feedSource.setDatasource(null);
                if (StringUtils.isBlank(feedSource.getDatasourceId())) {
                    feedSource.setDatasourceId(ds != null ? ds.getId() : null);
                }
            });
        }
        if (feed.getDestinations() != null) {
            feed.getDestinations().forEach(feedDestination -> {
                Datasource ds = serializeDatasource(feedDestination.getDatasource());
                feedDestination.setDatasource(null);
                if (StringUtils.isBlank(feedDestination.getDatasourceId())) {
                    feedDestination.setDatasourceId(ds != null ? ds.getId() : null);
                }
            });
        }
        feedMap.put(feed.getId(), feed);
    }

    private Datasource serializeDatasource(Datasource ds) {
        if (ds != null) {
            if (!datasourceMap.containsKey(ds.getId())) {
                datasourceMap.put(ds.getId(), ds);
                if (ds.getSourceForFeeds() != null) {
                    ds.getSourceForFeeds().forEach(sourceFeed -> {
                        Feed serializedFeed = feedMap.get(sourceFeed.getId());
                        if (serializedFeed == null) {
                            serialize(sourceFeed);
                        } else {
                            sourceFeed = serializedFeed;
                        }
                    });
                }

                if (ds.getDestinationForFeeds() != null) {
                    ds.getDestinationForFeeds().forEach(destFeed -> {
                        Feed serializedFeed = feedMap.get(destFeed.getId());
                        if (serializedFeed == null) {
                            serialize(destFeed);
                        } else {
                            destFeed = serializedFeed;
                        }
                    });
                }
            }
            return datasourceMap.get(ds.getId());

        }
        return null;

    }


    public Map<String, Feed> getFeedMap() {
        return feedMap;
    }

    public void setFeedMap(Map<String, Feed> feedMap) {
        this.feedMap = feedMap;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public Map<String, Datasource> getDatasourceMap() {
        return datasourceMap;
    }

    public void setDatasourceMap(Map<String, Datasource> datasourceMap) {
        this.datasourceMap = datasourceMap;
    }


    public Map<String, FeedLineageStyle> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, FeedLineageStyle> styles) {
        this.styles = styles;
    }
}