import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class wc4 {

    public static class MyMapper
            extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            // Skip header line (first line) of CSV
            if (value.toString().startsWith("created_at,tweet_id,")) {
                return;
            }

            String data[] = value.toString().split("(?:^|,)(?=[^\"]|(\")?)\"?((?(1)[^\"]*|[^,\"]*))\"?(?=,|$)", -1);
            int likes = 0;
            int retweets = 0;
            int[] sources = new int[3]; // 0: Web app, 1: , 2: iPhone, 3: Android

            // Likes
            try {
                likes = Integer.parseInt(data[3]);
            } catch (NumberFormatException e) {
                likes = 0;
            }

            // Retweets
            try {
                retweets = Integer.parseInt(data[4]);
            } catch (NumberFormatException e) {
                retweets = 0;
            }

            // Sources
            try {
                if (data[5].indexOf("Twitter Web App") > -1) sources[0] = 1;
                else if (data[5].indexOf("Twitter for iPhone") > -1) sources[1] = 1;
                else if (data[5].indexOf("Twitter for Android") > -1) sources[2] = 1;
            } catch (NumberFormatException e) {
                sources[0] = 0;
                sources[1] = 0;
                sources[2] = 0;
            }

            boolean trump = (data[2].indexOf("#DonaldTrump") > -1 || data[2].indexOf("#Trump") > -1);
            boolean biden = (data[2].indexOf("#JoeBiden") > -1 || data[2].indexOf("#Biden") > -1);

            Text result = new Text(String.valueOf(likes) + "," + String.valueOf(retweets) + ","
                    + String.valueOf(sources[0]) + "," + String.valueOf(sources[1]) + "," + String.valueOf(sources[2]));
            if (!trump && !biden) {
                return;
            }

            if (trump && biden) {
                context.write(new Text("Both"), result);
                return;
            }

            if (trump) {
                context.write(new Text("Trump"), result);
                return;
            }

            if (biden) {
                context.write(new Text("Biden"), result);
                return;
            }
        }
    }

    public static class MyReducer
            extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
            int likes = 0, retweets = 0;
            int iphone = 0, android = 0, webApp = 0;
            for (Text val : values) {
                String data[] = val.toString().split(",", -1);
                likes += Integer.parseInt(data[0]);
                retweets += Integer.parseInt(data[1]);
                webApp += Integer.parseInt(data[2]);
                iphone += Integer.parseInt(data[3]);
                android += Integer.parseInt(data[4]);

            }

            Text result = new Text(String.valueOf(likes) + ", " + String.valueOf(retweets) +
                    ", " + String.valueOf(webApp) + ", " + String.valueOf(iphone) + ", " + String.valueOf(android));
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count - 4");
        job.setJarByClass(wc4.class);
        job.setMapperClass(MyMapper.class);
        job.setCombinerClass(MyReducer.class);
        job.setReducerClass(MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}