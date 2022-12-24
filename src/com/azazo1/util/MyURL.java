package com.azazo1.util;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * toString 默认显示文件名称的 URL
 */
public record MyURL(@NotNull URL url) {
    /**
     * 只返回文件名
     */
    @Override
    public @NotNull String toString() {
        String path = url.getPath();
        return url.getPath()
                .substring(path.lastIndexOf('/', path.lastIndexOf('/') - 1) + 1) // 倒数两个路径,获得上一层的文件夹
                .substring(path.lastIndexOf('\\') + 1);
    }
}
