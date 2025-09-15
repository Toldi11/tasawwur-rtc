#ifndef TASAWWUR_JSON_HELPER_H
#define TASAWWUR_JSON_HELPER_H

#include <string>
#include <vector>
#include <unordered_map>

namespace tasawwur {

/**
 * Simple JSON value representation.
 * In production, use a proper JSON library like nlohmann/json.
 */
struct JsonValue {
    enum Type {
        NULL_VALUE,
        BOOLEAN,
        NUMBER,
        STRING,
        ARRAY,
        OBJECT
    };
    
    Type type = NULL_VALUE;
    bool bool_value = false;
    double number_value = 0.0;
    std::string string_value;
    std::vector<JsonValue> array_value;
    std::unordered_map<std::string, JsonValue> object_value;
    
    // Helper methods for object access
    std::string GetString(const std::string& key, const std::string& default_value = "") const;
    int GetInt(const std::string& key, int default_value = 0) const;
    bool GetBool(const std::string& key, bool default_value = false) const;
    std::vector<std::string> GetStringArray(const std::string& key) const;
};

/**
 * Simple JSON parser.
 * In production, use a proper JSON library.
 */
class JsonParser {
public:
    static JsonValue Parse(const std::string& json);
    
private:
    static JsonValue ParseObject(const std::string& json);
    static JsonValue ParseArray(const std::string& json);
    static std::string ParseString(const std::string& json);
    static size_t FindValueEnd(const std::string& str, size_t start);
    static std::string Trim(const std::string& str);
};

} // namespace tasawwur

#endif // TASAWWUR_JSON_HELPER_H

