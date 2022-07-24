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

public class wc6 {

    public static class MyMapper
            extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            if (value.toString().startsWith("created_at,tweet_id,")) {
                // Skip header line (first line) of CSV
                return;
            }
            String data[] = value.toString().split("(?:^|,)(?=[^\"]|(\")?)\"?((?(1)[^\"]*|[^,\"]*))\"?(?=,|$)", -1);
            boolean trump = (data[2].indexOf("#DonaldTrump") > -1 || data[2].indexOf("#Trump") > -1);
            boolean biden = (data[2].indexOf("#JoeBiden") > -1 || data[2].indexOf("#Biden") > -1);
            if (!trump && !biden) {
                return;
            }
            Text who;
            if (trump && biden)
                who = new Text("Both");
            else if (trump) {
                who = new Text("Trump");
            } else {
                who = new Text("Biden");
            }

            // Time
            // Case insensitive --> toLowerCase
            String state = data[18].toLowerCase();
            String[] createdAt = (data[0].split("\\s+"));
            String[] createdAt_time = createdAt[1].split(":");
            boolean time = false;
            // Between 9 am and 5 pm
            if (Integer.parseInt(createdAt_time[0]) > 9 && Integer.parseInt(createdAt_time[0]) < 17)
                time = true;

            // Get lng and lat
            float lat = 0;
            try {
                lat = Float.parseFloat(data[13]);
            } catch (NumberFormatException e) {
                lat = (float) -1e9;
            }
            float lng = 0;
            try {
                lng = Float.parseFloat(data[14]);
            } catch (NumberFormatException e) {
                lng = (float) -1e9;
            }

            // New york
            if (lng > -79.7624 && lng < -71.7517 && lat > 40.4772 && lat < 45.0153 && time)
                context.write(new Text("america"), who);
            // California
            if (lng > -124.6509 && lng < -114.1315 && lat > 32.5121 && lat < 42.0126 && time)
                context.write(new Text("france"), who);
        }
    }

    public static class MyReducer
            extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
            int trump = 0, biden = 0, both = 0;
            for (Text val : values) {
                String str = val.toString();
                if (str.equals("trump"))
                    trump++;
                if (str.equals("biden"))
                    biden++;
                if (str.equals("both"))
                    both++;
            }
            float all = (trump + biden + both);

            Text result = new Text(String.valueOf(both / all) + " " + String.valueOf(biden / all) + " "
                    + String.valueOf(trump / all) + String.valueOf(all));
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count - 6");
        job.setJarByClass(wc6.class);
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