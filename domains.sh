#!/bin/bash

# Variables
BASE_URL="https://data.commoncrawl.org/"
OUTPUT_DIR="./___downloaded_files"
EXTRACTED_DIR="./___extracted_files"
FINAL_OUTPUT="$2"
PATHS_FILE="$1" # Input .zip file containing the PATHS file

# Find the extracted PATHS file (assuming there's one)
if [[ -z "$PATHS_FILE" ]]; then
    echo "No PATHS file found in $PATHS_FILE."
    exit 1
fi
echo "Found PATHS file: $PATHS_FILE"

# Step 2: Create necessary directories
mkdir -p "$OUTPUT_DIR" "$EXTRACTED_DIR"

# Step 3: Download all files from the paths in the PATHS file
echo "Downloading files from $PATHS_FILE..."
count=0
while read -r path; do
    if [[ -n "$path" ]]; then
        # Generate the full URL
        url="${BASE_URL}${path}"
        
        # Download the file
        output_file="$OUTPUT_DIR/$count.gz"
        wget -q --show-progress -O "$output_file" "$url" || { echo "Failed to download $url"; exit 1; }
        
        # Extract the downloaded file
        extracted_file="$EXTRACTED_DIR/$count"
        gunzip -c "$output_file" > "$extracted_file" || { echo "Failed to extract $output_file"; exit 1; }

        rm $output_file

        cat $extracted_file >> "$FINAL_OUTPUT"

        rm $extracted_file
        
        echo "Processed file $count"
        ((count++))
    fi
done < "$PATHS_FILE"

# Clean up intermediate files
rm -r ./$EXTRACTED_DIR
rm -r ./$OUTPUT_DIR

echo "Combined output is in $FINAL_OUTPUT"
