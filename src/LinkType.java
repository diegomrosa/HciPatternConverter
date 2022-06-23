public enum LinkType {
    IS_ALTERNATIVE("is-alternative"),     // Significa que este padrão é uma solução alternativa para o mesmo problema.
    IS_CONTAINED_BY("is-contained-by"),    // Significa que este padrão é “menor” e é usado (com outros) para instanciar um padrão maior.
    CONTAINS("contains"),           // Significa o contrário de "is-contained-by", ou seja, este padrão é "maior" e usa o outro padrão (com outros) para ser instanciado.
    SPECIALIZATION("specialization"),     // Significa que este padrão é uma solução semelhante porém mais especializada que o outro padrão.
    GENERALIZATION("generalization"),     // Significa o contrário de "specialization", ou seja, este padrão é uma solução mais genérica que o outro padrão.
    IS_ALIAS("is-alias"),           // Significa que este padrão é idêntico ao outro padrão (dois padrões iguais apenas com nomes diferentes).
    NOT_RELATED("not-related");        // Significa que o link não indica nenhuma relação específica entre os dois padrões.

    String attributeValue;

    LinkType(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public static LinkType fromValue(String attributeValue) {
        for (LinkType type : LinkType.values()) {
            if (attributeValue.equals(type.getAttributeValue())) {
                return type;
            }
        }
        return null;
    }
}
