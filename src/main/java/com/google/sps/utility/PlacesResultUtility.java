package com.google.sps.utility;

import com.google.maps.model.PlacesSearchResponse;

public class PlacesResultUtility {
  /**
   * Gets place ID of first result from the response from the Google Places API. Scope of method is
   * public for testing purposes.
   *
   * @param response The PlacesSearchResponse object to Place IDs from
   */
  public static String getPlaceId(PlacesSearchResponse response) {
    if (response.results.length != 0) {
      return response.results[0].placeId;
    }
    return "";
  }
}
