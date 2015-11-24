import controllers.HttpsRedirectFilter
import play.api.mvc._

object Global extends WithFilters(HttpsRedirectFilter) {

}
