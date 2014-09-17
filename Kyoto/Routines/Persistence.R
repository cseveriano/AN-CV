Persistence <- function(gsiDate, gsi){

#Clear all workspace
#rm(list = ls(all = TRUE))

#dirClearSky = "../Data/ClearSky"
dirClearSky = "C:/Users/Carlos/Documents/Projetos Machine Learning/ANN-CV/CODES/Git/AN-CV/Kyoto/Data/ClearSky-15"


#receiving commands
#args <- commandArgs(trailingOnly = TRUE)
#gsiDate <- args[1]
#gsi <- numeric(args[2])

#rnorm(n=as.numeric(args[1]), mean=as.numeric(args[2]))

## 1 - Load ClearSky
csfiles = dir(dirClearSky)

csdata = NULL

for(x in csfiles)
{
    filepath = paste(dirClearSky, x, sep = "/")
    temp = read.table(filepath, header = TRUE, sep = "\t")
    csdata = rbind(csdata,temp)
}


## 2 - Calculate Persistence
#gsiDate = "2014-01-01 12:00"
#gsi = 11.8

if(length(grep("00:00:00",gsiDate))>0) {
  t = as.POSIXlt(gsiDate) - 1
  csDate = as.character(t)
}
else{
  csDate = gsiDate
}
csIndex = substr(csDate, 6, nchar(csDate)-3) # retrieve "mm-dd hh:mi" part

ind = grep(csIndex,csdata$Tm)
csky = csdata$CS_GHI[ind]

if(csky != 0) {
  Kt = gsi / csky
}
else{
  Kt = 0
}

ind = if(ind == length(csdata$CS_GHI[ind])) 1 else ind + 1
csky_next = csdata$CS_GHI[ind]
perst = Kt * csky_next;

return (c(gsiDate,csky, gsi, Kt, csky_next, perst ))
}








