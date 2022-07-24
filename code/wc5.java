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

public class wc5 {

    public static class MyMapper
            extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {

            // Skip header line (first line) of CSV
            if (value.toString().startsWith("created_at,tweet_id,")) {
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

            if (state.indexOf("new york") > -1 && time)
                context.write(new Text("america"), who);
            else if (state.indexOf("texas") > -1 && time)
                context.write(new Text("iran"), who);
            else if (state.indexOf("california") > -1 && time)
                context.write(new Text("netherlands"), who);
            else if (state.indexOf("florida") > -1 && time)
                context.write(new Text("austria"), who);
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
                    + String.valueOf(trump / all) +  String.valueOf(all));
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count - 5");
        job.setJarByClass(wc5.class);
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