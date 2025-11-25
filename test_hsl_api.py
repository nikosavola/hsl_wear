#!/usr/bin/env python3
"""
Simple script to test HSL Digitransit GraphQL API queries.
Usage: HSL_KEY=your_key python test_hsl_api.py
"""

import os
import json
import requests
from datetime import datetime

# API endpoints (matching GraphQLQueries.kt)
HSL_ENDPOINT_V2 = "https://api.digitransit.fi/routing/v2/hsl/gtfs/v1"
HSL_ENDPOINT_V1 = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"

def query_api(query, variables=None, use_v1=False):
    """Execute a GraphQL query against the HSL API."""
    endpoint = HSL_ENDPOINT_V1 if use_v1 else HSL_ENDPOINT_V2

    headers = {
        "Content-Type": "application/json",
    }

    # Add API key if provided
    api_key = os.environ.get("HSL_KEY")
    if api_key:
        headers["digitransit-subscription-key"] = api_key

    payload = {
        "query": query,
    }
    if variables:
        payload["variables"] = variables

    response = requests.post(endpoint, json=payload, headers=headers)
    response.raise_for_status()
    return response.json()

def test_stop_with_zones():
    """Test querying a stop to see zone information."""
    query = """
    {
      stop(id: "HSL:1040129") {
        name
        code
        zoneId
        platformCode
        desc
        lat
        lon
      }
    }
    """
    print("Testing stop query with zone information (v1 API)...")
    print(f"Query: {query}\n")

    result = query_api(query, use_v1=True)
    print("Result:")
    print(json.dumps(result, indent=2))
    return result

def test_route_planning():
    """Test route planning query with zone information."""
    # Kamppi to Pasila (common route for testing)
    from_lat, from_lon = 60.16892, 24.93120
    to_lat, to_lon = 60.19885, 24.93376

    now = datetime.now()
    date = now.strftime("%Y-%m-%d")
    time = now.strftime("%H:%M:%S")

    query = """
    query Plan($fromLat: Float!, $fromLon: Float!, $toLat: Float!, $toLon: Float!, $date: String!, $time: String!) {
      plan(
        from: {lat: $fromLat, lon: $fromLon}
        to: {lat: $toLat, lon: $toLon}
        date: $date
        time: $time
        numItineraries: 3
      ) {
        itineraries {
          duration
          walkDistance
          legs {
            mode
            startTime
            duration
            distance
            from {
              name
              lat
              lon
              stop {
                name
                code
                gtfsId
                platformCode
                zoneId
              }
            }
            to {
              name
              lat
              lon
              stop {
                name
                code
                gtfsId
                platformCode
                zoneId
              }
            }
            route {
              shortName
            }
            trip {
              tripHeadsign
            }
          }
        }
      }
    }
    """

    variables = {
        "fromLat": from_lat,
        "fromLon": from_lon,
        "toLat": to_lat,
        "toLon": to_lon,
        "date": date,
        "time": time
    }

    print("\nTesting route planning query with zones (v2 API)...")
    print(f"From: {from_lat}, {from_lon}")
    print(f"To: {to_lat}, {to_lon}")
    print(f"Date/Time: {date} {time}\n")

    result = query_api(query, variables)  # uses v2 by default
    print("Result:")
    print(json.dumps(result, indent=2))
    return result

if __name__ == "__main__":
    api_key = os.environ.get("HSL_KEY")
    if api_key:
        print(f"Using API key: {api_key[:8]}...")
    else:
        print("No HSL_KEY found, trying without authentication")

    print("=" * 60)

    try:
        # Only test route planning with v2 (v1 is deprecated)
        test_route_planning()

    except requests.exceptions.HTTPError as e:
        print(f"HTTP Error: {e}")
        print(f"Response: {e.response.text}")
    except Exception as e:
        print(f"Error: {e}")
