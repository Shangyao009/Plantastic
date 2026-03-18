#!/usr/bin/env python3
"""
API Connection Test Script for Plantastic

Usage:
    python test.py

This script tests the API accessibility by sending a simple chat completion request.
"""

import requests
import json
import sys

def test_api(base_url: str, api_key: str, model: str) -> bool:
    """
    Test API connection with the given parameters.

    Args:
        base_url: API base URL (e.g., https://api.openai.com/)
        api_key: API key for authentication
        model: Model name (e.g., gpt-4o)

    Returns:
        True if successful, False otherwise
    """
    # Ensure base_url ends with /
    if not base_url.endswith('/'):
        base_url += '/'

    endpoint = f"{base_url}v1/chat/completions"

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": model,
        "messages": [
            {
                "role": "user",
                "content": "Say 'API is working!' in a short response."
            }
        ],
        "max_tokens": 50
    }

    print(f"\nTesting API Connection")
    print(f"-" * 40)
    print(f"Base URL: {base_url}")
    print(f"Model: {model}")
    print(f"Endpoint: {endpoint}")
    print("-" * 40)

    try:
        print("\nSending request...")
        response = requests.post(
            endpoint,
            headers=headers,
            json=payload,
            timeout=30
        )

        print(f"Status Code: {response.status_code}")

        if response.status_code == 200:
            print("\n\u2705 SUCCESS: API is accessible and working!")
            try:
                data = response.json()
                content = data.get("choices", [{}])[0].get("message", {}).get("content", "")
                print(f"Response: {content}")
            except json.JSONDecodeError:
                print(f"Response: {response.text[:200]}")
            return True

        elif response.status_code == 401:
            print("\n\u274C ERROR: Authentication failed (401 Unauthorized)")
            print("Check your API key and make sure it's valid.")
            print(f"Response: {response.text[:500]}")

        elif response.status_code == 403:
            print("\n\u274C ERROR: Access forbidden (403 Forbidden)")
            print("Your API key may not have access to this model.")
            print(f"Response: {response.text[:500]}")

        elif response.status_code == 404:
            print("\n\u274C ERROR: Endpoint not found (404 Not Found)")
            print(f"Check if the endpoint '{endpoint}' is correct.")
            print(f"Response: {response.text[:500]}")

        elif response.status_code == 429:
            print("\n\u274C ERROR: Rate limit exceeded (429 Too Many Requests)")
            print("Please wait a moment and try again.")
            print(f"Response: {response.text[:500]}")

        else:
            print(f"\n\u274C ERROR: Request failed with status code {response.status_code}")
            print(f"Response: {response.text[:500]}")

    except requests.exceptions.Timeout:
        print("\n\u274C ERROR: Request timed out")
        print("The server took too long to respond. Check your network connection.")

    except requests.exceptions.ConnectionError as e:
        print(f"\n\u274C ERROR: Connection failed")
        print(f"Could not connect to {base_url}")
        print(f"Error: {str(e)[:200]}")
        print("\nCheck if:")
        print("  - The URL is correct")
        print("  - Your internet connection is working")
        print("  - The server is running")

    except requests.exceptions.RequestException as e:
        print(f"\n\u274C ERROR: Request failed")
        print(f"Error: {str(e)}")

    return False


def main():
    print("=" * 50)
    print("  Plantastic API Connection Test")
    print("=" * 50)

    # Get API configuration from user
    print("\nEnter your API configuration:\n")

    base_url = input("Base URL (e.g., https://api.openai.com/): ").strip()
    if not base_url:
        base_url = "https://api.openai.com/"
        print(f"Using default: {base_url}")

    api_key = input("API Key: ").strip()
    if not api_key:
        print("ERROR: API key is required!")
        sys.exit(1)

    model = input("Model (e.g., gpt-4o, gpt-4-turbo): ").strip()
    if not model:
        model = "gpt-4o"
        print(f"Using default: {model}")

    # Run the test
    success = test_api(base_url, api_key, model)

    print("\n" + "=" * 50)
    if success:
        print("API TEST PASSED!")
    else:
        print("API TEST FAILED!")
    print("=" * 50)

    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
