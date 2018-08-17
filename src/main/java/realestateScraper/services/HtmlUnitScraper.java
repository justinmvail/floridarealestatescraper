package realestateScraper.services;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.commons.logging.LogFactory;

import java.util.logging.Level;

abstract class HtmlUnitScraper {

    void setScraperLogging(WebClient webClient, boolean useLogging){
        if(!useLogging) {
            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

            webClient.setIncorrectnessListener((arg0, arg1) -> {
                // intentionally left blank to override default functionality with nothing
            });
            webClient.setCssErrorHandler(new CSSErrorHandler() {
                @Override
                public void warning(CSSParseException exception) throws CSSException {
                    // intentionally left blank to override default functionality with nothing
                }
                @Override
                public void fatalError(CSSParseException exception) throws CSSException {
                    // intentionally left blank to override default functionality with nothing
                }
                @Override
                public void error(CSSParseException exception) throws CSSException {
                    // intentionally left blank to override default functionality with nothing
                }
            });
        }
    }

}
