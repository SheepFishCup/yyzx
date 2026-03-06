// vue.config.js
module.exports = {
    // 开发服务器代理
    devServer: {
        host:'0.0.0.0',
        proxy: {
            '/api': {
                target: 'http://localhost:9999',  // 后端地址
                changeOrigin: true,
                pathRewrite: {
                    '^/api': ''
                }
            },
        }
    },
    
    // 生产环境配置
    publicPath: process.env.NODE_ENV === 'production' ? '/' : '/',
    lintOnSave: process.env.NODE_ENV === 'development',
    // 输出目录
    outputDir: 'dist',
    
    // 静态资源目录
    assetsDir: 'static'
}