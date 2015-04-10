#################################################################################
#### function to calculate sun position, extraterrestial irradiance and clear sky
#################################################################################
calZen <- function(Tm, lat, lon, tz, interval)
{
	jd = JD(Tm-interval*60)
	sunv = sunvector(jd, lat, lon, tz)
	zen = sunpos(sunv)[,2]
	dec = declination(jd)*pi/180
	re = 1.000110+0.034221*cos(dec)+0.001280*sin(dec)+0.00719*cos(2*dec)+0.000077*sin(2*dec)
	Ioh = 1362*re*cos(zen*pi/180)
	Ioh[Ioh<0]=0
	########################################################################
	#### Singapore local clear sky model
	#### Reference: D. Yang, W. Walsh, P. Jirutitijaroen, Estimation of clear
	####			sky global horizontal irradiance at the equator, J. Sol.
	####			Energy Eng., (2014) doi: 10.1115/1.4027264
	########################################################################
	Ics = 0.8298*1362*re*cos(zen*pi/180)^1.3585*exp(-0.00135*(90-zen))
	Ics[is.nan(Ics)] = 0
	out = list(zen, Ioh, Ics, re)
	names(out) = c("zenith", "extraterrestrial", "clearsky", "re")
	out
}



