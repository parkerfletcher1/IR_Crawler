import os
from warcio.archiveiterator import ArchiveIterator

def extract_wet_file(wet_file_path, output_folder):
    """
    Extracts plaintext content from a WET WARC file to a specified folder.
    """
    if not os.path.exists(output_folder):
        os.makedirs(output_folder) # Create the output folder if it doesn't exist

    with open(wet_file_path, 'rb') as stream:
        for record in ArchiveIterator(stream):
            # WET files usually contain 'conversion' records with plaintext payload
            if record.rec_type == 'conversion':
                # Get the target URI to use as part of the filename
                uri = record.rec_headers.get_header('WARC-Target-URI')
                if uri:
                    # Sanitize the URI to create a valid filename
                    filename = "".join(c for c in uri if c.isalnum() or c in ('.', '_', '-')).rstrip()
                    # Shorten filename if it's too long
                    if len(filename) > 100:
                        filename = filename[:100] + ".txt"
                    else:
                        filename += ".txt"
                    
                    filepath = os.path.join(output_folder, filename)
                    
                    try:
                        # Read the content stream (plaintext)
                        content = record.content_stream().read().decode('utf-8')
                        
                        with open(filepath, 'w', encoding='utf-8') as output_file:
                            output_file.write(content)
                        print(f"Saved: {filepath}")
                    except Exception as e:
                        print(f"Error saving {filepath}: {e}")

# Example usage:
# Replace 'path/to/your/file.warc.wet.gz' with your WET file path
# Replace 'output_texts' with your desired output folder name
extract_wet_file('your_file.warc.wet.gz', 'output_texts')
