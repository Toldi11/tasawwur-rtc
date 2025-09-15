#include "json_helper.h"
#include <sstream>
#include <algorithm>

namespace tasawwur {

// Simple JSON parser implementation (for demonstration purposes)
// In a production system, you would use a proper JSON library like nlohmann/json

JsonValue JsonParser::Parse(const std::string& json) {
    JsonValue result;
    std::string trimmed = Trim(json);
    
    if (trimmed.empty()) {
        return result;
    }
    
    if (trimmed[0] == '{') {
        result = ParseObject(trimmed);
    } else if (trimmed[0] == '[') {
        result = ParseArray(trimmed);
    } else if (trimmed[0] == '"') {
        result.type = JsonValue::STRING;
        result.string_value = ParseString(trimmed);
    } else if (trimmed == "true" || trimmed == "false") {
        result.type = JsonValue::BOOLEAN;
        result.bool_value = (trimmed == "true");
    } else if (trimmed == "null") {
        result.type = JsonValue::NULL_VALUE;
    } else {
        // Try to parse as number
        try {
            if (trimmed.find('.') != std::string::npos) {
                result.type = JsonValue::NUMBER;
                result.number_value = std::stod(trimmed);
            } else {
                result.type = JsonValue::NUMBER;
                result.number_value = std::stoi(trimmed);
            }
        } catch (...) {
            // Invalid JSON
            result.type = JsonValue::NULL_VALUE;
        }
    }
    
    return result;
}

JsonValue JsonParser::ParseObject(const std::string& json) {
    JsonValue result;
    result.type = JsonValue::OBJECT;
    
    std::string content = json.substr(1, json.length() - 2); // Remove { and }
    content = Trim(content);
    
    if (content.empty()) {
        return result;
    }
    
    // Simple parsing - in production, use a proper JSON library
    size_t pos = 0;
    while (pos < content.length()) {
        // Find key
        size_t key_start = content.find('"', pos);
        if (key_start == std::string::npos) break;
        
        size_t key_end = content.find('"', key_start + 1);
        if (key_end == std::string::npos) break;
        
        std::string key = content.substr(key_start + 1, key_end - key_start - 1);
        
        // Find colon
        size_t colon = content.find(':', key_end);
        if (colon == std::string::npos) break;
        
        // Find value
        pos = colon + 1;
        while (pos < content.length() && std::isspace(content[pos])) pos++;
        
        size_t value_end = FindValueEnd(content, pos);
        std::string value_str = content.substr(pos, value_end - pos);
        value_str = Trim(value_str);
        
        JsonValue value = Parse(value_str);
        result.object_value[key] = value;
        
        pos = value_end;
        // Skip comma
        size_t comma = content.find(',', pos);
        if (comma != std::string::npos) {
            pos = comma + 1;
        } else {
            break;
        }
    }
    
    return result;
}

JsonValue JsonParser::ParseArray(const std::string& json) {
    JsonValue result;
    result.type = JsonValue::ARRAY;
    
    std::string content = json.substr(1, json.length() - 2); // Remove [ and ]
    content = Trim(content);
    
    if (content.empty()) {
        return result;
    }
    
    size_t pos = 0;
    while (pos < content.length()) {
        while (pos < content.length() && std::isspace(content[pos])) pos++;
        
        size_t value_end = FindValueEnd(content, pos);
        std::string value_str = content.substr(pos, value_end - pos);
        value_str = Trim(value_str);
        
        JsonValue value = Parse(value_str);
        result.array_value.push_back(value);
        
        pos = value_end;
        // Skip comma
        size_t comma = content.find(',', pos);
        if (comma != std::string::npos) {
            pos = comma + 1;
        } else {
            break;
        }
    }
    
    return result;
}

std::string JsonParser::ParseString(const std::string& json) {
    if (json.length() < 2 || json[0] != '"' || json.back() != '"') {
        return "";
    }
    
    return json.substr(1, json.length() - 2);
}

size_t JsonParser::FindValueEnd(const std::string& str, size_t start) {
    if (start >= str.length()) return start;
    
    char first_char = str[start];
    
    if (first_char == '"') {
        // String value
        size_t end = str.find('"', start + 1);
        return (end != std::string::npos) ? end + 1 : str.length();
    } else if (first_char == '{') {
        // Object value
        int brace_count = 1;
        size_t pos = start + 1;
        while (pos < str.length() && brace_count > 0) {
            if (str[pos] == '{') brace_count++;
            else if (str[pos] == '}') brace_count--;
            pos++;
        }
        return pos;
    } else if (first_char == '[') {
        // Array value
        int bracket_count = 1;
        size_t pos = start + 1;
        while (pos < str.length() && bracket_count > 0) {
            if (str[pos] == '[') bracket_count++;
            else if (str[pos] == ']') bracket_count--;
            pos++;
        }
        return pos;
    } else {
        // Primitive value
        size_t pos = start;
        while (pos < str.length() && str[pos] != ',' && str[pos] != '}' && str[pos] != ']') {
            pos++;
        }
        return pos;
    }
}

std::string JsonParser::Trim(const std::string& str) {
    size_t start = str.find_first_not_of(" \t\n\r");
    if (start == std::string::npos) return "";
    
    size_t end = str.find_last_not_of(" \t\n\r");
    return str.substr(start, end - start + 1);
}

std::string JsonValue::GetString(const std::string& key, const std::string& default_value) const {
    if (type != OBJECT) return default_value;
    
    auto it = object_value.find(key);
    if (it != object_value.end() && it->second.type == STRING) {
        return it->second.string_value;
    }
    
    return default_value;
}

int JsonValue::GetInt(const std::string& key, int default_value) const {
    if (type != OBJECT) return default_value;
    
    auto it = object_value.find(key);
    if (it != object_value.end() && it->second.type == NUMBER) {
        return static_cast<int>(it->second.number_value);
    }
    
    return default_value;
}

bool JsonValue::GetBool(const std::string& key, bool default_value) const {
    if (type != OBJECT) return default_value;
    
    auto it = object_value.find(key);
    if (it != object_value.end() && it->second.type == BOOLEAN) {
        return it->second.bool_value;
    }
    
    return default_value;
}

std::vector<std::string> JsonValue::GetStringArray(const std::string& key) const {
    std::vector<std::string> result;
    
    if (type != OBJECT) return result;
    
    auto it = object_value.find(key);
    if (it != object_value.end() && it->second.type == ARRAY) {
        for (const auto& item : it->second.array_value) {
            if (item.type == STRING) {
                result.push_back(item.string_value);
            }
        }
    }
    
    return result;
}

} // namespace tasawwur

