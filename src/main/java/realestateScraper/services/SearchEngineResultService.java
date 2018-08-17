package realestateScraper.services;

import java.io.IOException;

public interface SearchEngineResultService {

    public String getUrlForSearchResults (String searchPhrase) throws IOException;
}
