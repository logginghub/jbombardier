var path = require("path");

module.exports = {

    entry: {
        app: ["./app/main.js"]
    },

    module: {
        loaders: [
            {test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/font-woff'},
            {test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/octet-stream'},
            {test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'file'},
            {test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=image/svg+xml'},

            // {test: /\.css$/, loader: "style-loader!css-loader" },
            {test: /\.(png|jpg)$/, loader: 'url-loader?limit=8192' },
            // {test: /\.html$/, loader: "html" }
        ]
    },

    output: {
        path: path.resolve(__dirname, "build"),
        publicPath: "/assets/",
        filename: "bundle.js"
    },

    devServer: {
        proxy: {
            '/services/*': {
                target: 'http://127.0.0.1:8888', secure: false
            }
        }
    }

};