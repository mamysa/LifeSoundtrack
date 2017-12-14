package ch.usi.inf.gabrialex.musicplayer2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import ch.usi.inf.gabrialex.datastructures.EnvironmentContext;

/**
 * Created by usi on 08.12.17.
 */

public class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

    @Override
    protected Weather doInBackground(String... params) {
        Weather weather = new Weather();
        String data = ((new WeatherHttpClient()).getWeatherData(params[0], params[1]));

        try {
            weather = JSONWeatherParser.getWeather(data);

            // Let's retrieve the icon
            //weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;

    }
    @Override
    protected void onPostExecute(Weather weather) {
        super.onPostExecute(weather);
        EnvironmentContext.getInstance().setWeather(weather.currentCondition.getCondition());
        Log.d("WEATHER", EnvironmentContext.getInstance().getWeather());
    }

}