import os
import re
import xml.etree.ElementTree as ET

# Register namespaces to preserve correct prefixes during write-back
ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')
ET.register_namespace('app', 'http://schemas.android.com/apk/res-auto')
ET.register_namespace('tools', 'http://schemas.android.com/tools')

layout_dir = os.path.join("app", "src", "main", "res", "layout")
strings_xml_path = os.path.join("app", "src", "main", "res", "values", "strings.xml")

# Read existing strings to avoid duplicate values
existing_strings = {}
string_keys_by_value = {}

print("Parsing strings.xml...")
try:
    tree = ET.parse(strings_xml_path)
    root = tree.getroot()
    for string_elem in root.findall('string'):
        name = string_elem.attrib.get('name')
        val = string_elem.text
        if name and val:
            existing_strings[name] = val
            string_keys_by_value[val.strip()] = name
except Exception as e:
    print(f"Error parsing strings.xml: {e}")

# Helper to generate a clean string name
def generate_string_key(val, file_name):
    cleaned = val.lower()
    cleaned = re.sub(r'[\u2600-\u27BF\u1F300-\u1F9FF\u2B50\u23F0\u23F3\U0001F000-\U0001FFFF]', '', cleaned) # remove emoji
    cleaned = re.sub(r'[^a-z0-9_]', ' ', cleaned) # replace non-alphanumeric with spaces
    cleaned = cleaned.strip()
    words = cleaned.split()
    
    # Take first 4 words, join with underscore
    key_name = "_".join(words[:4])
    
    if not key_name:
        base_file = os.path.splitext(file_name)[0]
        key_name = f"txt_{base_file}_{len(existing_strings)}"
    
    if key_name[0].isdigit():
        key_name = "txt_" + key_name
        
    original_key = key_name
    counter = 1
    while key_name in existing_strings:
        key_name = f"{original_key}_{counter}"
        counter += 1
        
    return key_name

new_strings_to_add = {}

new_strings_to_add['cd_close'] = "Tutup"
new_strings_to_add['cd_send'] = "Kirim pesan"
new_strings_to_add['cd_emergency_call'] = "Hubungi darurat"
new_strings_to_add['cd_back'] = "Kembali"

string_keys_by_value["Tutup"] = "cd_close"
string_keys_by_value["Kirim pesan"] = "cd_send"
string_keys_by_value["Hubungi darurat"] = "cd_emergency_call"
string_keys_by_value["Kembali"] = "cd_back"

for k, v in new_strings_to_add.items():
    existing_strings[k] = v

def refactor_file(file_path, file):
    modified = False
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        def refactor_element(elem):
            nonlocal modified
            tag_name = elem.tag.split("}")[-1]
            
            # 1. Handle hardcoded text, hint, title, label attributes
            for attr_key, attr_val in list(elem.attrib.items()):
                local_attr = attr_key.split("}")[-1]
                if local_attr in ["text", "hint", "title", "label"]:
                    if attr_val and not attr_val.startswith("@string/") and not attr_val.startswith("@android:string/"):
                        if not attr_val.startswith("@{") and not attr_val.isdigit() and attr_val.strip() != "":
                            val_stripped = attr_val.strip()
                            
                            if val_stripped in string_keys_by_value:
                                key = string_keys_by_value[val_stripped]
                            else:
                                key = generate_string_key(val_stripped, file)
                                new_strings_to_add[key] = attr_val
                                existing_strings[key] = attr_val
                                string_keys_by_value[val_stripped] = key
                            
                            elem.attrib[attr_key] = f"@string/{key}"
                            modified = True
            
            # 2. Handle missing contentDescription for ImageView and ImageButton
            if tag_name in ["ImageView", "ImageButton"]:
                cd = elem.attrib.get("{http://schemas.android.com/apk/res/android}contentDescription")
                if cd is None:
                    if tag_name == "ImageButton":
                        btn_id = elem.attrib.get("{http://schemas.android.com/apk/res/android}id", "")
                        if "close" in btn_id.lower() or "cancel" in btn_id.lower() or "btn_delete" in btn_id.lower():
                            elem.attrib["{http://schemas.android.com/apk/res/android}contentDescription"] = "@string/cd_close"
                        elif "send" in btn_id.lower():
                            elem.attrib["{http://schemas.android.com/apk/res/android}contentDescription"] = "@string/cd_send"
                        elif "emergency" in btn_id.lower() or "call" in btn_id.lower():
                            elem.attrib["{http://schemas.android.com/apk/res/android}contentDescription"] = "@string/cd_emergency_call"
                        elif "back" in btn_id.lower():
                            elem.attrib["{http://schemas.android.com/apk/res/android}contentDescription"] = "@string/cd_back"
                        else:
                            elem.attrib["{http://schemas.android.com/apk/res/android}contentDescription"] = "@null"
                    else:
                        elem.attrib["{http://schemas.android.com/apk/res/android}contentDescription"] = "@null"
                    modified = True
            
            for child in elem:
                refactor_element(child)
        
        refactor_element(root)
        
        if modified:
            tree.write(file_path, encoding="utf-8", xml_declaration=True)
            print(f"Refactored: {file}")
            
    except Exception as e:
        print(f"Error refactoring {file}: {e}")

print("Refactoring layout files...")
for root_dir, _, files in os.walk(layout_dir):
    for file in files:
        if not file.endswith(".xml"):
            continue
        file_path = os.path.join(root_dir, file)
        refactor_file(file_path, file)

# Append new strings to strings.xml
if new_strings_to_add:
    print(f"Adding {len(new_strings_to_add)} new strings to strings.xml...")
    try:
        with open(strings_xml_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        closing_idx = content.rfind("</resources>")
        if closing_idx != -1:
            appended_content = "\n    <!-- Auto-generated Strings via refactor_resources.py -->\n"
            for k, v in new_strings_to_add.items():
                escaped_v = v.replace("'", "\\'").replace('"', '\\"').replace("&", "&amp;").replace("\n", "\\n")
                appended_content += f'    <string name="{k}">{escaped_v}</string>\n'
            
            new_content = content[:closing_idx] + appended_content + content[closing_idx:]
            with open(strings_xml_path, "w", encoding="utf-8") as f:
                f.write(new_content)
            print("Successfully updated strings.xml")
        else:
            print("Error: Could not locate closing </resources> tag in strings.xml")
    except Exception as e:
        print(f"Error updating strings.xml: {e}")
else:
    print("No new strings to add.")
