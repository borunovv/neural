package com.borunovv.common;

import java.io.InputStreamReader;

/**
 * @author borunovv
 */
public final class ResourceUtils {

    /**
     * Вернет Reader на ресурсный файл.
     * Не забудь его закрыть потом!.
     * Либо пользуй "try-with-resources"-идеологию:
     * try (Reader trainReader = ResourceUtils.getFile("/titanic/train.csv")
     * {
     *   ...
     * }
     * @param relativeToResourcesFilePath - Путь относительно ресурсов, пример: "/titanic/train.csv"
     *                                    Директроия ресурсов в исходниках: [project_dir]/src/main/resources
     */
    public static InputStreamReader getFile(String relativeToResourcesFilePath) {
        return new InputStreamReader(
                ResourceUtils.class.getResourceAsStream(relativeToResourcesFilePath));
    }

}
